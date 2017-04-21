/*
 * Copyright 2016 Rik van der Kleij
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

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine._
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

import scala.collection.JavaConverters._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.SyncVar
import scala.concurrent.duration._
import scala.io._
import scala.sys.process._

private[repl] abstract class StackReplProcess(val project: Project, val extraStartOptions: Seq[String] = Seq(), val isProjectRepl: Boolean = false) extends ProjectComponent {

  private final val LineSeparator = '\n'

  private[this] var available = false

  private[this] val outputStream = new SyncVar[OutputStream]

  private[this] val stdOut = new LinkedBlockingDeque[String]

  private[this] val stdErr = new LinkedBlockingDeque[String]

  private final val LoadTimeout = 60.seconds
  private final val DefaultTimeout = 5.seconds

  private final val EndOfOutputIndicator = "^IntellijHaskell^"

  private final val DelayBetweenReads = 1.millis

  protected def execute(command: String): Option[StackReplOutput] = {

    if (!available) {
      logWarning(s"Stack repl is not yet available. Command was: $command")
      return None
    }

    if (!outputStream.isSet) {
      logError("Can not write to Stack repl. Check if your Haskell/Stack environment is working okay")
      return None
    }

    try {
      stdOut.clear()
      stdErr.clear()

      val stdOutResult = new ArrayBuffer[String]
      val stdErrResult = new ArrayBuffer[String]

      def reachedEndOfOutput: Boolean = stdOutResult.lastOption.exists(_.startsWith(EndOfOutputIndicator)) && (command.startsWith(":module") || command.startsWith(":set") || stdOutResult.length > 1 || stdErrResult.length > 1)

      writeToOutputStream(command)

      val timeout = if (command.startsWith(":load")) LoadTimeout else DefaultTimeout
      val deadline = timeout.fromNow
      while (deadline.hasTimeLeft && (stdOutResult.isEmpty || !reachedEndOfOutput)) {
        stdOut.drainTo(stdOutResult.asJava)
        stdErr.drainTo(stdErrResult.asJava)

        // We have to wait...
        Thread.sleep(DelayBetweenReads.toMillis)
      }

      if (!reachedEndOfOutput) {
        logError(s"No result from Stack repl within $timeout. Command was: $command")

        None
      } else {
        stdOut.drainTo(stdOutResult.asJava)
        stdErr.drainTo(stdErrResult.asJava)

        logInfo("command: " + command)
        logInfo("stdOut: " + stdOutResult.mkString("\n"))
        logInfo("errOut: " + stdErrResult.mkString("\n"))

        Some(StackReplOutput(convertOutputToOneMessagePerLine(project, removePrompt(stdOutResult)), convertOutputToOneMessagePerLine(project, stdErrResult)))
      }
    }
    catch {
      case _: InterruptedException =>
        logWarning("Interruped exception while executing command: " + command)
        None
      case e: Exception =>
        logError(s"Error in communication with Stack repl: ${e.getMessage}. Check if your Haskell/Stack environment is working okay. Command was: $command")
        exit()
        None
    }
  }

  def start(): Unit = {

    def writeOutputToLogInfo(): Unit = {
      if (!stdOut.isEmpty) {
        logInfo(stdOut.asScala.mkString("\n"))
      }

      if (!stdErr.isEmpty) {
        logInfo(stdErr.asScala.mkString("\n"))
      }
    }

    if (available) {
      logError("Stack repl can not be started because it's already running or busy with starting")
      return
    }

    if (isProjectRepl) {
      executeBuild(project, Seq("build", "intero"), "build of Intero")
      executeBuild(project, Seq("build", "--test", "--only-dependencies", "--haddock", "--fast"), "build of dependencies")
    }

    HaskellSdkType.getStackPath(project).foreach(stackPath => {
      try {
        val command = (Seq(stackPath, "repl", "--with-ghc", "intero", "--verbosity", "warn", "--no-build", "--terminal", "--no-load", "--ghci-options=-ignore-dot-ghci") ++ extraStartOptions).mkString(" ")

        logInfo(s"Stack repl will be started with command: $command")

        val process = Option(EnvironmentUtil.getEnvironmentMap) match {
          case None => Process(command, new File(project.getBasePath))
          case Some(envMap) => Process(command, new File(project.getBasePath), envMap.asScala.toArray: _*)
        }
        process.run(
          new ProcessIO(
            in => outputStream.put(in),
            (out: InputStream) => Source.fromInputStream(out).getLines.foreach(stdOut.add),
            (err: InputStream) => Source.fromInputStream(err).getLines.foreach(stdErr.add)
          ))

        // We have to wait so repl has started before continue
        Thread.sleep(1000)

        writeOutputToLogInfo()
        stdOut.clear()
        stdErr.clear()

        writeToOutputStream(s""":set prompt "$EndOfOutputIndicator\\n"""")

        available = true
        logInfo("Stack repl is started.")

        if (isProjectRepl) {
          execute(":set -Wall")
          execute(":set -fdefer-typed-holes")

          HaskellProjectUtil.findCabalPackageName(project) match {
            case Some(name) =>
              val packageModuleName = s"Paths_${name.replaceAll("-", "_")}"
              logInfo(s"Package module `$packageModuleName` will be loaded")
              execute(s":load $packageModuleName")
            case None =>
              logInfo(s"Package module will not be loaded")
          }
        }

      }
      catch {
        case e: Exception =>
          logError("Could not start Stack repl. Make sure you have set right path to Stack in settings.")
          logError(s"Error message while trying to start Stack repl: ${e.getMessage}")
          exit(forceExit = true)
      }
    })
  }

  def exit(forceExit: Boolean = false): Unit = {
    if (!forceExit && !available) {
      logWarning("Stack repl can not be stopped because it's already stopped or busy with stopping")
      return
    }

    try {
      try {
        if (outputStream.isSet) {
          writeToOutputStream(":q")
        }
        if (outputStream.isSet) {
          outputStream.take(100).close()
        }
      }
      catch {
        case e: Exception =>
          logError(s"Error while shutting down Stack repl for project ${project.getName}. Error message: ${e.getMessage}")
      }
      if (stdin != null) {
        stdin.close()
      }
      if (stdout != null) {
        stdout.close()
      }
      if (stderr != null) {
        stderr.close()
      }
    } finally {
      if (outputStream.isSet) {
        try {
          outputStream.take(100).close()
        } catch {
          case _: Exception => ()
        }
      }
      available = false
    }
    if (forceExit) {
      logError("Stack repl is stopped and is not restarted automatically. Use `Tools`/`Restart Haskell Stack REPLs`")
    } else {
      logInfo("Stack repl is stopped")
    }
  }

  def restart(): Unit = {
    exit()
    start()
  }

  private def logError(message: String) = {
    HaskellNotificationGroup.logErrorEvent(project, s"[$getComponentName] $message")
  }

  private def logWarning(message: String) = {
    HaskellNotificationGroup.logWarningEvent(project, s"[$getComponentName] $message")
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
    joinIndentedLines(project, output.filterNot(_.isEmpty))
  }

  private def joinIndentedLines(project: Project, lines: Seq[String]): Seq[String] = {
    if (lines.size == 1) {
      lines
    } else {
      try {
        lines.foldLeft(ListBuffer[StringBuilder]())((lb, s) =>
          if (s.startsWith("  ")) {
            lb.last.append(s)
            lb
          }
          else {
            lb += new StringBuilder(2, s)
          }).map(_.toString)
      } catch {
        case _: NoSuchElementException =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not join indented lines. Probably first line started with spaces. Unexpected input was: ${lines.mkString(", ")}")
          Seq()
      }
    }
  }

  override def projectOpened(): Unit = {}

  override def projectClosed(): Unit = if (HaskellProjectUtil.isHaskellStackProject(project)) exit()

  override def initComponent(): Unit = {}

  override def disposeComponent(): Unit = {}
}

case class StackReplOutput(stdOutLines: Seq[String] = Seq(), stdErrLines: Seq[String] = Seq())
