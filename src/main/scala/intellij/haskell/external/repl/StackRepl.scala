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
import java.util.concurrent.LinkedBlockingDeque

import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.{GhcVersion, HaskellEditorUtil, HaskellProjectUtil, StringUtil}

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

  private[this] val outputStream = new SyncVar[OutputStream]

  private[this] val stdOut = new LinkedBlockingDeque[String]

  private[this] val stdErr = new LinkedBlockingDeque[String]

  private final val LoadTimeout = 60.seconds

  private final val DefaultTimeout = replTimeout.seconds

  private final val EndOfOutputIndicator = "^IntellijHaskell^"

  private final val DelayBetweenReads = 1.millis

  private final val ExitCommand = ":q"

  private final val CanNotSatisfyErrorMessage = "cannot satisfy"

  def getComponentName: String = target.map(t => "project-stack-repl-" + t).getOrElse("global-stack-repl")

  protected def execute(command: String, forceExecute: Boolean = false): Option[StackReplOutput] = {
    if (!available && !forceExecute) {
      HaskellEditorUtil.showStatusBarNotificationBalloon(project, s"[$getComponentName] Haskell support is not available when Stack repl is not running.")
      None
    } else if (!outputStream.isSet) {
      logError("Can not write to Stack repl. Check if your Stack project environment is working okay")
      None
    } else {
      stdOut.clear()
      stdErr.clear()

      val stdOutResult = new ArrayBuffer[String]
      val stdErrResult = new ArrayBuffer[String]

      def drain() = {
        stdOut.drainTo(stdOutResult.asJava)
        stdErr.drainTo(stdErrResult.asJava)
      }

      try {
        def reachedEndOfOutput: Boolean =
          if (command == ExitCommand) {
            stdOutResult.lastOption.exists(_.startsWith("Leaving GHCi"))
          } else {
            stdOutResult.lastOption.exists(_.contains(EndOfOutputIndicator)) && (command.startsWith(":module") || command.startsWith(":set") || stdOutResult.length > 1 || stdErrResult.length > 1)
          }

        writeToOutputStream(command)

        val timeout = if (command.startsWith(":load")) LoadTimeout
        else DefaultTimeout

        val deadline = timeout.fromNow
        while (deadline.hasTimeLeft && (stdOutResult.isEmpty || !reachedEndOfOutput)) {
          drain()

          // We have to wait...
          Thread.sleep(DelayBetweenReads.toMillis)
        }

        drain()

        logInfo("command: " + command)

        if (reachedEndOfOutput) {
          if (stdOutResult.nonEmpty) logInfo("stdout: " + stdOutResult.mkString("\n"))
          if (stdErrResult.nonEmpty) logInfo("stderr: " + stdErrResult.mkString("\n"))

          Some(StackReplOutput(convertOutputToOneMessagePerLine(project, removePrompt(stdOutResult)), convertOutputToOneMessagePerLine(project, stdErrResult)))
        } else {
          drain()
          stdErrResult.find(_.contains(CanNotSatisfyErrorMessage)) match {
            case Some(error) =>
              val message = s"Could not start Stack repl for target `${target.getOrElse("-")}` because dependency has build errors ${error.replaceAll(CanNotSatisfyErrorMessage, "")}"
              logInfo(message)
              HaskellEditorUtil.showStatusBarNotificationBalloon(project, message)
            case None =>
              logError(s"No result from Stack repl within $timeout. Command was: $command")
              logOutput(stdOutResult, stdErrResult)
          }
          None
        }
      }
      catch {
        case _: InterruptedException =>
          logWarning("Interrupted exception while executing command: " + command)
          None
        case e: Exception =>
          logError(s"Error in communication with Stack repl: ${e.getMessage}. Check if your Haskell/Stack environment is working okay. Command was: $command")
          drain()
          logOutput(stdOutResult, stdErrResult)
          exit()
          None
      }
    }
  }

  private def logOutput(stdOutResult: ArrayBuffer[String], stdErrResult: ArrayBuffer[String]): Unit = {
    if (stdOutResult.nonEmpty) logInfo("stdout: " + stdOutResult.mkString("\n"))
    if (stdErrResult.nonEmpty) logError("stderr: " + stdErrResult.mkString("\n"))
  }

  def start(): Unit = synchronized {

    def writeOutputToLog(): Unit = {
      if (!stdOut.isEmpty) {
        logInfo(stdOut.asScala.mkString("\n"))
      }

      if (!stdErr.isEmpty) {
        stdErr.asScala.foreach(l => {
          if (l.startsWith("Configuring GHCi with") || l.startsWith("The following GHC options are incompatible with GHCi")) {
            logInfo(l)
          } else {
            logError(l)
          }
        })
      }
    }

    if (available) {
      logError("Stack repl can not be started because it's already running")
    } else {
      HaskellSdkType.getStackPath(project).foreach(stackPath => {
        try {
          val extraOptions = if (stanzaType.isEmpty || stanzaType.contains(TestSuiteType)) {
            extraReplOptions ++ Seq("--test")
          } else if (stanzaType.isEmpty || stanzaType.contains(BenchmarkType)) {
            extraReplOptions ++ Seq("--bench")
          } else {
            extraReplOptions
          }

          val command = (Seq(stackPath, "repl") ++ target.toSeq ++ Seq("--with-ghc", "intero", "--no-load", "--no-build", "--ghci-options=-ignore-dot-ghci", "--silent") ++ extraOptions).mkString(" ")
          logInfo(s"Stack repl will be started with command: $command")
          val processBuilder = Option(EnvironmentUtil.getEnvironmentMap) match {
            case None => Process(command, new File(project.getBasePath))
            case Some(envMap) => Process(command, new File(project.getBasePath), envMap.asScala.toArray: _*)
          }

          val process = processBuilder.run(
            new ProcessIO(
              in => outputStream.put(in),
              (out: InputStream) => Source.fromInputStream(out).getLines.foreach(stdOut.add),
              (err: InputStream) => Source.fromInputStream(err).getLines.foreach(stdErr.add)
            ))

          def shouldBeStarted: Boolean = {
            process.isAlive() && stdOut.size() > 2
          }

          val deadline = DefaultTimeout.fromNow
          while (deadline.hasTimeLeft && !shouldBeStarted) {
            // We have to wait till repl is started
            Thread.sleep(DelayBetweenReads.toMillis)
          }

          if (shouldBeStarted) {
            val output = execute(s""":set prompt "$EndOfOutputIndicator\\n"""", forceExecute = true)
            if (output.isEmpty) {
              closeResources()
              logInfo("Stack repl is not started because of problem")
            } else {
              if (stanzaType != None) {
                execute(":set -fdefer-typed-holes", forceExecute = true)
                HaskellProjectUtil.getGhcVersion(project).foreach { ghcVersion =>
                  if (ghcVersion >= GhcVersion(8, 2, 1)) {
                    execute(":set -fno-diagnostics-show-caret", forceExecute = true)
                  }
                }
              }

              logInfo("Stack repl is started")
              available = true
            }
          } else {
            logError(s"Stack repl could not be started within $DefaultTimeout")
            closeResources()
            writeOutputToLog()
          }
        }
        catch {
          case e: Exception =>
            logError("Could not start Stack repl. Make sure you have set right path to Stack in settings")
            logError(s"Error message while trying to start Stack repl: ${e.getMessage}")
            writeOutputToLog()
            exit(forceExit = true)
        }
      })
    }
  }

  def exit(forceExit: Boolean = false): Unit = synchronized {
    if (!available && !forceExit) {
      logInfo("Stack repl can not be stopped because it's already stopped")
    } else {
      try {
        available = false
        execute(ExitCommand, forceExecute = true)
      }
      catch {
        case e: Exception =>
          logError(s"Error while shutting down Stack repl for project ${project.getName}. Error message: ${e.getMessage}")
      }
      closeResources()
      logInfo("Stack repl is stopped")
    }
  }

  private def closeResources() = {
    try {
      closeResource(stdin)
      closeResource(stdout)
      closeResource(stderr)
    } finally {
      if (outputStream.isSet) {
        try {
          outputStream.take(100).close()
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

  private def logWarning(message: String) = {
    HaskellNotificationGroup.logWarningBalloonEvent(project, s"[$getComponentName] $message")
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

  private def writeToOutputStream(command: String) = {
    outputStream.get.write(command.getBytes)
    outputStream.get.write(LineSeparator)
    outputStream.get.flush()
  }

  private def convertOutputToOneMessagePerLine(project: Project, output: Seq[String]) = {
    StringUtil.joinIndentedLines(project, output.filterNot(_.isEmpty))
  }
}

case class StackReplOutput(stdOutLines: Seq[String] = Seq(), stdErrLines: Seq[String] = Seq())

sealed trait StanzaType

case object LibType extends StanzaType

case object ExeType extends StanzaType

case object TestSuiteType extends StanzaType

case object BenchmarkType extends StanzaType
