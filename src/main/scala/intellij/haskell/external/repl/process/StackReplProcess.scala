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

package intellij.haskell.external.repl.process

import java.io._
import java.util.concurrent.LinkedBlockingDeque

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util._

import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.SyncVar
import scala.concurrent.duration._
import scala.io._
import scala.sys.process._

abstract class StackReplProcess(val project: Project, val extraCommandOptions: Seq[String] = Seq()) extends ProjectComponent {

  private final val LineSeparatorInBytes = OSUtil.LineSeparator

  private[this] var available = false

  private[this] val outputStream = new SyncVar[OutputStream]()

  private[this] val stdOut = new SyncVar[LinkedBlockingDeque[String]]()
  stdOut.put(new LinkedBlockingDeque[String])

  private[this] val stdErr = new SyncVar[LinkedBlockingDeque[String]]()
  stdErr.put(new LinkedBlockingDeque[String])

  private final val Timeout = 1.seconds

  // Only experience with Linux and OSX for now
  private final val DelayBetweenReads = if (OSUtil.isOSX || OSUtil.isWindows) 80 else 40

  protected def execute(command: String, waitCondition: Option[WaitCondition] = Some(StdOutput)): StackReplOutput = synchronized {

    var previousWait = true

    def waitForOutput(stdOutResult: java.util.List[String], stdErrResult: java.util.List[String]): Boolean = {
      val wait = waitCondition match {
        case Some(StdOutput) =>
          if (getStdErr.isEmpty && stdErrResult.isEmpty)
            stdOutResult.isEmpty || !getStdOut.isEmpty
          else
            !getStdErr.isEmpty
        case Some(StdOutputForInfo) =>
          if (getStdErr.isEmpty && stdErrResult.isEmpty)
            !getStdOut.isEmpty || !(stdOutResult.lastOption.exists(_.contains("--")) || stdOutResult.lastOption.exists(_.startsWith("infix")))
          else
            !getStdErr.isEmpty
        case Some(StdErrForLoad) => (!stdOutResult.lastOption.exists(l => l.contains("Collecting type info") || l.contains("Failed"))) || !getStdErr.isEmpty
        case _ => !getStdErr.isEmpty
      }

      // Wait one more time in case wait is false and previous wait is true. So do not wait longer if wait is two times false directly after each other
      val waitResult = wait || previousWait
      previousWait = wait
      waitResult
    }

    if (!available) {
      logInfo(s"Stack repl is not yet available. Command was: $command")
      return StackReplOutput()
    }

    if (!outputStream.isSet) {
      logError("Can not write to Stack repl. Check if your Haskell/Stack environment is working okay")
      return StackReplOutput()
    }

    try {
      getStdOut.clear()
      getStdErr.clear()
      writeToOutputStream(command)

      val stdOutResult = new ArrayBuffer[String]
      val stdErrResult = new ArrayBuffer[String]

      val deadline = Timeout.fromNow

      while (deadline.hasTimeLeft && waitForOutput(stdOutResult, stdErrResult)) {
        getStdOut.drainTo(stdOutResult)
        getStdErr.drainTo(stdErrResult)

        // We have to wait...
        Thread.sleep(DelayBetweenReads)
      }

      getStdOut.drainTo(stdOutResult)
      getStdErr.drainTo(stdErrResult)

      if (deadline.isOverdue()) {
        logError(s"No result from Stack repl within $Timeout. Command was: $command")
      }

      logInfo("command: " + command)
      logInfo("stdOut: " + stdOutResult.mkString("\n"))
      logInfo("errOut: " + stdErrResult.mkString("\n"))

      if (deadline.isOverdue()) {
        StackReplOutput()
      } else {
        StackReplOutput(convertOutputToOneMessagePerLine(removePrompt(stdOutResult)), convertOutputToOneMessagePerLine(stdErrResult))
      }
    }
    catch {
      case e: Exception =>
        logError(s"Error in communication with Stack repl: ${e.getMessage}. Check if your Haskell/Stack environment is working okay. Command was: $command")
        exit()
        StackReplOutput()
    }
  }

  def start(): Unit = {
    if (available) {
      logError("Stack repl can not be started because it's already running or busy with starting")
      return
    }

    HaskellSdkType.getStackPath(project).foreach { stackPath =>
      try {
        val command = (Seq(stackPath, "repl", "--with-ghc", "intero", "--verbosity", "warn", "--fast", "--no-load",
          "--force-dirty", "--ghc-options", "-v1", "--test", "--terminal") ++ extraCommandOptions).mkString(" ")
        logInfo(s"Stack repl will be started with command: $command")
        val process = getEnvParameters match {
          case None => Process(command, new File(project.getBasePath))
          case Some(ep) => Process(command, new File(project.getBasePath), ep.toArray: _*)
        }
        process.run(
          new ProcessIO(
            in => outputStream.put(in),
            (out: InputStream) => getStdOut.addAll(Source.fromInputStream(out).getLines.toSeq),
            (err: InputStream) => getStdErr.addAll(Source.fromInputStream(err).getLines.toSeq)
          ))

        getStdOut.clear()
        getStdErr.clear()

        val stdOutResult = new ArrayBuffer[String]
        val stdErrResult = new ArrayBuffer[String]

        while (stdOutResult.isEmpty || !getStdOut.isEmpty || !getStdErr.isEmpty) {
          if (getStdOut.nonEmpty) {
            logInfo(getStdOut.mkString("\n"))
          }

          if (getStdErr.nonEmpty) {
            logInfo(getStdErr.mkString("\n"))
          }

          getStdOut.drainTo(stdOutResult)
          getStdErr.drainTo(stdErrResult)

          // We have to wait...
          Thread.sleep(200)
        }

        getStdOut.drainTo(stdOutResult)
        getStdErr.drainTo(stdErrResult)

        available = true

        logInfo("Stack repl is started.")
      }
      catch {
        case e: Exception =>
          logError("Could not start Stack repl. Make sure you have set right path to Stack in settings.")
          logError(s"Error message while trying to start Stack repl: ${e.getMessage}")
          exit()
      }
    }
  }

  def exit(): Unit = {
    if (!available) {
      logError("Stack repl can not be stopped because it's already stopped or busy with stopping")
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
        outputStream.take(500)
      }
      available = false
    }
    logInfo("Stack repl is stopped.")
  }

  private def getStdErr = stdErr.get

  private def getStdOut = stdOut.get

  private def logError(message: String) = {
    HaskellNotificationGroup.logError(s"[$getComponentName] $message")
  }

  private def logInfo(message: String) = {
    HaskellNotificationGroup.logInfo(s"[$getComponentName] $message")
  }

  private def removePrompt(output: Seq[String]): Seq[String] = {
    output.headOption.map(e => e.replaceFirst("""([\\*\w\s\\.]+>)+""", "")) match {
      case Some(e) => e +: output.tail
      case None => output
    }
  }

  private def writeToOutputStream(command: String) = {
    outputStream.get.write(command.getBytes)
    outputStream.get.write(LineSeparatorInBytes)
    outputStream.get.flush()
  }

  private def getEnvParameters: Option[java.util.Map[String, String]] = {
    if (OSUtil.isOSX) {
      Option(EnvironmentUtil.getEnvironmentMap)
    } else {
      None
    }
  }

  private def convertOutputToOneMessagePerLine(output: Seq[String]) = {
    joinIndentedLines(output.filterNot(_.isEmpty))
  }

  private def joinIndentedLines(lines: Seq[String]): Seq[String] = {
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
        case e: NoSuchElementException =>
          HaskellNotificationGroup.notifyBalloonWarning(s"Could not join indented lines. Probably first line started with spaces. Unexpected input was: ${lines.mkString(", ")}")
          Seq()
      }
    }
  }

  override def projectOpened(): Unit = {}

  override def projectClosed(): Unit = exit()

  override def initComponent(): Unit = {}

  override def disposeComponent(): Unit = {}

  protected abstract class WaitCondition

  protected case object StdOutput extends WaitCondition

  protected case object StdOutputForInfo extends WaitCondition

  protected case object StdErrForLoad extends WaitCondition

}

case class StackReplOutput(stdOutLines: Seq[String] = Seq(), stdErrLines: Seq[String] = Seq())
