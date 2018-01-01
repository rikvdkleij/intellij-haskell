/*
 * Copyright 2014-2017 Rik van der Kleij
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
import intellij.haskell.external.repl.StackRepl.{BenchmarkType, StackReplOutput, StanzaType, TestSuiteType}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.{GhcVersion, HaskellEditorUtil, HaskellProjectUtil, StringUtil}
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.SyncVar
import scala.concurrent.duration._
import scala.io._
import scala.sys.process._

abstract class StackRepl(val project: Project, var stanzaType: Option[StanzaType], var target: Option[String], extraReplOptions: Seq[String] = Seq(), replTimeout: Int) {

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

  private final val DelayBetweenReads = 1.millis

  private final val ExitCommand = ":q"

  private final val CanNotSatisfyErrorMessageIndicator = "cannot satisfy"

  def getComponentName: String = target.map(t => "project-stack-repl-" + t).getOrElse("global-stack-repl")

  protected def execute(command: String, forceExecute: Boolean = false): Option[StackReplOutput] = {

    if (!available && !forceExecute) {
      HaskellEditorUtil.showStatusBarInfoMessage(project, s"[$getComponentName] Haskell support is not available when Stack REPL is not running.")
      None
    } else if (!outputStreamSyncVar.isSet) {
      logError("Can not write to Stack repl. Check if your Stack project environment is working okay")
      None
    } else {
      stdoutQueue.clear()
      stderrQueue.clear()

      val stdoutResult = new ArrayBuffer[String]
      val stderrResult = new ArrayBuffer[String]

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

      try {

        def hasReachedEndOfOutput =
          if (command == ExitCommand) {
            stdoutResult.lastOption.exists(_.startsWith("Leaving GHCi"))
          } else {
            stdoutResult.lastOption.exists(_.contains(EndOfOutputIndicator)) && (command.startsWith(":module") || command.startsWith(":set") || stdoutResult.length > 1 || stderrResult.length > 1)
          }

        def writeToOutputStream(command: String) = {
          val output = outputStreamSyncVar.get
          output.write(command.getBytes)
          output.write(LineSeparator)
          output.flush()
        }

        writeToOutputStream(command)

        val timeout = if (command.startsWith(":load")) LoadTimeout else DefaultTimeout

        val deadline = timeout.fromNow
        while (deadline.hasTimeLeft && !hasReachedEndOfOutput) {
          drainQueues()

          // We have to wait...
          Thread.sleep(DelayBetweenReads.toMillis)
        }

        drainQueues()

        logInfo("command: " + command)
        logOutput(errorAsInfo = true)

        if (hasReachedEndOfOutput) {
          Some(StackReplOutput(convertOutputToOneMessagePerLine(project, removePrompt(stdoutResult)), convertOutputToOneMessagePerLine(project, stderrResult)))
        } else {
          drainQueues()
          logError(s"No result from Stack REPL within $timeout. Command was: $command")
          None
        }
      }
      catch {
        case e: Exception =>
          logError(s"Error in communication with Stack repl: ${e.getMessage}. Check if your Haskell/Stack environment is working okay. Command was: $command")
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

    if (available) {
      logError("Stack REPL can not be started because it's already running")
    } else {
      HaskellSdkType.getStackPath(project).foreach(stackPath => {
        try {
          starting = true
          val extraOptions = if (stanzaType.isEmpty || stanzaType.contains(TestSuiteType)) {
            extraReplOptions ++ Seq("--test")
          } else if (stanzaType.isEmpty || stanzaType.contains(BenchmarkType)) {
            extraReplOptions ++ Seq("--bench")
          } else {
            extraReplOptions
          }

          val replGhciOptionsFilePath = createGhciOptionsFile.getAbsolutePath
          val command = (Seq(stackPath, "repl") ++ target.toSeq ++ Seq("--with-ghc", "intero", "--no-load", "--no-build", "--ghci-options", s"-ghci-script=$replGhciOptionsFilePath", "--silent") ++ extraOptions).mkString(" ")

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
            Thread.sleep(DelayBetweenReads.toMillis)
          }

          if (isStarted) {
            if (stanzaType != None) {
              execute(":set -fdefer-typed-holes", forceExecute = true)
              HaskellProjectUtil.getGhcVersion(project).foreach { ghcVersion =>
                if (ghcVersion >= GhcVersion(8, 2, 1)) {
                  execute(":set -fno-diagnostics-show-caret", forceExecute = true)
                }
              }
            }
            logInfo("Stack REPL is started")
            available = true
          } else {
            if (hasDependencyError) {
              logInfo(s"Stack REPL could not be started for target `${target.getOrElse("-")}` because a dependency has build errors")
              HaskellEditorUtil.showStatusBarNotificationBalloon(project, s"Stack REPL could not be started for target `${target.getOrElse("-")}` because a dependency has build errors")
            } else {
              logError(s"Stack REPL could not be started within $DefaultTimeout")
              writeOutputToLog()
            }
            exit(forceExit = true)
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
      }
      closeResources()
      logInfo("Stack REPL is stopped")
    }
  }


  def createGhciOptionsFile: File = {
    val ghciOptionsFile = new File(GlobalInfo.getIntelliJHaskellDirectory, "repl.ghci")
    if (!ghciOptionsFile.exists()) {
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

  def restart(): Unit = {
    exit()
    start()
  }

  private def logError(message: String) = {
    HaskellNotificationGroup.logErrorBalloonEvent(project, s"[$getComponentName] $message")
  }

  private def logInfo(message: String) = {
    HaskellNotificationGroup.logInfoEvent(project, s"[$getComponentName] $message")
  }

  private def removePrompt(output: Seq[String]): Seq[String] = {
    if (output.isEmpty) {
      output
    } else {
      output.init
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