/*
 * Copyright 2014-2018 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.external.repl

import java.io._
import java.util.concurrent.LinkedBlockingQueue

import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import intellij.haskell.external.component.ProjectLibraryFileWatcher
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.{BenchmarkType, StackReplOutput, TestSuiteType}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil, HaskellProjectUtil, StringUtil}
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.SyncVar
import scala.concurrent.duration._
import scala.io._
import scala.sys.process._

abstract class StackRepl(project: Project, componentInfo: Option[StackComponentInfo], extraReplOptions: Seq[String] = Seq(), replTimeout: Int) {

  private val stanzaType = componentInfo.map(_.stanzaType)

  private object GhciCommand {

    trait Command

    case object Load extends Command

    case object LocalBrowse extends Command

    case object Set extends Command

    case object Module extends Command

    case object OtherCommand extends Command

  }

  private final val LineSeparator = '\n'

  @volatile
  private[external] var available = false

  @volatile
  private[external] var starting = false

  private[this] val outputStreamSyncVar = new SyncVar[OutputStream]

  private[this] val stdoutQueue = new LinkedBlockingQueue[String]

  private[this] val stderrQueue = new LinkedBlockingQueue[String]

  private final val LoadTimeout = 60.seconds

  private final val DefaultTimeout = replTimeout.seconds

  private final val EndOfOutputIndicator = "^IntellijHaskell^"

  private final val DelayBetweenReadsInMillis = 1

  private final val ExitCommand = ":q"

  private final val CanNotSatisfyErrorMessageIndicator = "cannot satisfy"

  private final val LocalBrowseStopReadingIndicator = "-- imported via"

  def getComponentName: String = componentInfo.map(_.target).map(t => "project-stack-repl-" + t).getOrElse("global-stack-repl")

  private val stdoutResult = new ArrayBuffer[String]
  private val stderrResult = new ArrayBuffer[String]

  protected def execute(command: String, forceExecute: Boolean = false): Option[StackReplOutput] = {

    if ((!available || starting) && !forceExecute) {
      HaskellEditorUtil.showStatusBarMessage(project, s"[$getComponentName] Haskell support is not available when Stack REPL is not (yet) running.")
      None
    } else if (!outputStreamSyncVar.isSet) {
      logError("Can not write to Stack repl. Check if your Stack project environment is working okay")
      None
    } else {

      def init() = {
        stdoutQueue.clear()
        stderrQueue.clear()

        stdoutResult.clear()
        stderrResult.clear()
      }

      def logOutput(errorAsInfo: Boolean = false): Unit = {
        if (stdoutResult.nonEmpty) logInfo("stdout: " + stdoutResult.mkString("\n"))
        if (stderrResult.nonEmpty) {
          val stderrMessage = "stderr: " + stderrResult.mkString("\n")
          if (errorAsInfo) {
            logInfo(stderrMessage)
          } else {
            logError(stderrMessage)
          }
        }
      }

      def drainQueues() = {
        stdoutQueue.drainTo(stdoutResult.asJava)
        stderrQueue.drainTo(stderrResult.asJava)
      }

      def cleanupOutputPreviousCommand(): Unit = {
        drainQueues()

        if (stdoutResult.nonEmpty) {
          // Output of previous command is there and could still being written....
          val deadline = DefaultTimeout.fromNow
          while (deadline.hasTimeLeft() && !stdoutResult.lastOption.exists(_.contains(EndOfOutputIndicator))) {
            drainQueues()

            // We have to wait...
            Thread.sleep(DelayBetweenReadsInMillis)
          }
        }
      }

      try {
        stdoutResult.clear()
        stderrResult.clear()

        cleanupOutputPreviousCommand()

        init()

        val ghciCommand = command match {
          case c if c.startsWith(":browse! *") => GhciCommand.LocalBrowse
          case c if c.startsWith(":load") | c.startsWith(":reload") => GhciCommand.Load
          case c if c.startsWith(":module") => GhciCommand.Module
          case c if c.startsWith(":set") => GhciCommand.Set
          case _ => GhciCommand.OtherCommand
        }

        def outputContainsEndOfOutputIndicator = {
          stdoutResult.lastOption.exists(_.contains(EndOfOutputIndicator))
        }

        def hasReachedEndOfOutput =
          if (command == ExitCommand) {
            stdoutResult.lastOption.exists(_.startsWith("Leaving GHCi"))
          } else if (ghciCommand == GhciCommand.LocalBrowse) {
            stdoutResult.exists(_.startsWith(LocalBrowseStopReadingIndicator)) || outputContainsEndOfOutputIndicator || stderrResult.nonEmpty
          } else {
            outputContainsEndOfOutputIndicator && (ghciCommand == GhciCommand.Module || ghciCommand == GhciCommand.Set || stdoutResult.length > 1 || stderrResult.nonEmpty)
          }

        def writeToOutputStream(command: String) = {
          val output = outputStreamSyncVar.get
          output.write(command.getBytes)
          output.write(LineSeparator)
          output.flush()
        }

        writeToOutputStream(command)

        val timeout = if (ghciCommand == GhciCommand.Load) LoadTimeout else DefaultTimeout

        val deadline = timeout.fromNow
        while (deadline.hasTimeLeft && !hasReachedEndOfOutput) {
          drainQueues()

          // We have to wait...
          Thread.sleep(DelayBetweenReadsInMillis)
        }

        if (deadline.hasTimeLeft) {
          logInfo(s"Command $command took + ${(timeout - deadline.timeLeft).toMillis} ms")
          logOutput(errorAsInfo = true)
          val result = if (ghciCommand == GhciCommand.LocalBrowse) {
            stdoutResult.takeWhile(l => !l.startsWith(LocalBrowseStopReadingIndicator))
          } else {
            stdoutResult
          }
          Some(StackReplOutput(convertOutputToOneMessagePerLine(project, removePrompt(result)), convertOutputToOneMessagePerLine(project, stderrResult)))
        }
        else {
          drainQueues()
          logError(s"No result from Stack REPL within $timeout. Command was: $command")
          exit(forceExit = true)
          None
        }
      }
      catch {
        case e: Exception =>
          logError(s"Error in communication with Stack repl: ${e.getMessage}. Check if your Haskell/Stack environment is working okay. Command was: `$command`")
          drainQueues()
          logOutput()
          exit()
          None
      }
    }
  }


  def start(): Unit = synchronized {

    def writeOutputToLog(): Unit = {
      if (!stdoutQueue.isEmpty) {
        logInfo(stdoutQueue.asScala.mkString("\n"))
      }

      if (!stderrQueue.isEmpty) {
        stderrQueue.asScala.foreach(l => {
          if (l.startsWith("Configuring GHCi with") || l.startsWith("The following GHC options are incompatible with GHCi")) {
            logInfo(l)
          } else {
            logError(l)
          }
        })
      }
    }

    if (available || starting) {
      logError("Stack REPL can not be started because it's already starting or running")
    } else {
      starting = true
      HaskellSdkType.getStackPath(project).foreach(stackPath => {
        try {
          val extraOptions = if (stanzaType.contains(TestSuiteType)) {
            extraReplOptions ++ Seq("--test")
          } else if (stanzaType.contains(BenchmarkType)) {
            extraReplOptions ++ Seq("--bench")
          } else if (stanzaType.isEmpty) {
            extraReplOptions ++ Seq("--test") ++ Seq("--bench")
          } else {
            extraReplOptions
          }

          val replGhciOptionsFilePath = createGhciOptionsFile.getAbsolutePath
          val command = (Seq(stackPath, "repl") ++
            componentInfo.map(_.target).toSeq ++
            Seq("--with-ghc", "intero", "--no-load", "--no-build", "--ghci-options", s"-ghci-script=$replGhciOptionsFilePath", "--silent", "--ghc-options", "-v1") ++ extraOptions).mkString(" ")

          logInfo(s"Stack REPL will be started with command: $command")

          val processBuilder = Option(EnvironmentUtil.getEnvironmentMap) match {
            case None => Process(command, new File(project.getBasePath))
            case Some(envMap) => Process(command, new File(project.getBasePath), envMap.asScala.toArray: _*)
          }

          stdoutQueue.clear()
          stderrQueue.clear()

          val process = processBuilder.run(
            new ProcessIO(
              in => outputStreamSyncVar.put(in),
              (out: InputStream) => Source.fromInputStream(out).getLines.foreach(stdoutQueue.add),
              (err: InputStream) => Source.fromInputStream(err).getLines.foreach(stderrQueue.add)
            ))

          def isStarted = {
            process.isAlive() && stdoutQueue.toArray[String](Array()).lastOption.exists(_.contains(EndOfOutputIndicator))
          }

          def hasDependencyError = {
            stderrQueue.toArray.mkString(" ").contains(CanNotSatisfyErrorMessageIndicator)
          }

          val deadline = DefaultTimeout.fromNow
          while (deadline.hasTimeLeft && !isStarted && !hasDependencyError) {
            // We have to wait till REPL is started
            Thread.sleep(DelayBetweenReadsInMillis)
          }

          if (isStarted && !hasDependencyError) {
            if (stanzaType.isDefined) {
              execute(":set -fdefer-type-errors", forceExecute = true)
              if (HaskellProjectUtil.setNoDiagnosticsShowCaretFlag(project)) {
                execute(s":set ${StackCommandLine.NoDiagnosticsShowCaretFlag}", forceExecute = true)
              }
            }
            logInfo("Stack REPL is started")
            available = true
          } else {
            if (hasDependencyError) {
              if (!ProjectLibraryFileWatcher.isBuilding(project)) {
                val target = componentInfo.map(_.target).getOrElse("-")
                val error = stderrQueue.asScala.headOption.map(_.replace("<command line>:", "").trim).getOrElse("a dependency failed to build")
                val message = s"Stack REPL could not be started for target `$target` because $error"
                logInfo(message)
                HaskellEditorUtil.showStatusBarBalloonMessage(project, message)
              }
            } else {
              logError(s"Stack REPL could not be started within $DefaultTimeout")
              writeOutputToLog()
            }
            closeResources()
          }
        }
        catch {
          case e: Exception =>
            logError("Could not start Stack REPL. Make sure you have set right path to Stack in Settings")
            logError(s"Error message while trying to start Stack REPL: ${e.getMessage}")
            writeOutputToLog()
            exit(forceExit = true)
        }
        finally {
          starting = false
        }
      })
    }
  }

  def exit(forceExit: Boolean = false): Unit = synchronized {
    if (!available && !forceExit) {
      logInfo("Stack REPL can not be stopped because it's already stopped")
    } else {
      try {
        available = false
        execute(ExitCommand, forceExecute = true)
      }
      catch {
        case e: Exception =>
          logError(s"Error while shutting down Stack REPL for project ${project.getName}. Error message: ${e.getMessage}")
      } finally {
        closeResources()
      }
      logInfo("Stack REPL is stopped")
    }
  }

  private def createGhciOptionsFile: File = {
    val ghciOptionsFile = new File(GlobalInfo.getIntelliJHaskellDirectory, "repl.ghci")
    if (!ghciOptionsFile.exists()) {
      ghciOptionsFile.createNewFile()
      ghciOptionsFile.setWritable(true, true)
      HaskellFileUtil.removeGroupWritePermission(ghciOptionsFile)

      val writer = new BufferedWriter(new FileWriter(ghciOptionsFile))
      try {
        writer.write(s""":set prompt "$EndOfOutputIndicator\\n"""")
      } finally {
        writer.close()
      }
    }
    ghciOptionsFile
  }

  private def closeResources() = {
    try {
      closeResource(stdin)
      closeResource(stdout)
      closeResource(stderr)
    } finally {
      if (outputStreamSyncVar.isSet) {
        try {
          outputStreamSyncVar.take(100).close()
        } catch {
          case _: Exception => ()
        }
      }
    }
  }

  private def closeResource(closeable: Closeable) = {
    try {
      if (closeable != null) {
        closeable.close()
      }
    } catch {
      case _: IOException => ()
    }
  }

  def restart(forceExit: Boolean = false): Unit

  private def logError(message: String) = {
    HaskellNotificationGroup.logErrorBalloonEvent(project, s"[$getComponentName] $message")
  }

  private def logInfo(message: String) = {
    HaskellNotificationGroup.logInfoEvent(project, s"[$getComponentName] $message")
  }

  private def removePrompt(output: Seq[String]): Seq[String] = {
    if (output.lastOption.exists(_.trim == EndOfOutputIndicator)) {
      output.init
    } else {
      output
    }
  }

  private def convertOutputToOneMessagePerLine(project: Project, output: Seq[String]) = {
    StringUtil.joinIndentedLines(project, output.filterNot(_.isEmpty))
  }
}

object StackRepl {

  case class StackReplOutput(stdoutLines: Seq[String] = Seq(), stderrLines: Seq[String] = Seq())

  sealed trait StanzaType

  case object LibType extends StanzaType

  case object ExeType extends StanzaType

  case object TestSuiteType extends StanzaType

  case object BenchmarkType extends StanzaType

}