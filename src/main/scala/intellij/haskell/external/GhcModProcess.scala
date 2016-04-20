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

package intellij.haskell.external

import java.io._
import java.util.concurrent.Executors

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io._
import scala.sys.process._

abstract class GhcModProcess(val project: Project) extends ProjectComponent {

  private final val ExecutorService = Executors.newSingleThreadExecutor
  implicit private final val ExecContext = ExecutionContext.fromExecutorService(ExecutorService)

  private final val LineSeparatorInBytes = OSUtil.LineSeparator

  private[this] var outputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private final val OK = "OK"

  private final val TimeOut = 5000L
  private final val GhcModErrorIndicator = "NG"
  private var ghcModProblemTime: Option[Long] = None

  def execute(command: String): GhcModOutput = synchronized {
    if (ghcModProblemTime.exists(problemTime => System.currentTimeMillis - problemTime < TimeOut)) {
      return GhcModOutput()
    }

    if (outputStream == null) {
      start()
    }

    if (outputStream != null) {
      try {
        writeToOutputStream(command)

        val waitForStdOutput = Future {
          while (!stdOutListBuffer.lastOption.contains(OK) && !stdOutListBuffer.headOption.exists(_.startsWith(GhcModErrorIndicator))) {
            // wait for result
            Thread.sleep(5)
          }
          stdOutListBuffer
        }
        val stdOutput = Await.result(waitForStdOutput, 5.second)

        if (stdOutput.headOption.exists(_.startsWith(GhcModErrorIndicator))) {
          HaskellNotificationGroup.notifyError(s"ghc-mod error output: ${stdOutput.mkString(" ")}")
          GhcModOutput()
        } else {
          GhcModOutput(stdOutput.init)
        }
      }
      catch {
        case e: Exception =>
          HaskellNotificationGroup.notifyError(s"Error in communication with ghc-mod: ${e.getMessage}. Check if GHC SDK is set and ghc-mod is okay. ghc-mod will not be called for 5 seconds. Command was: $command")
          setGhcModProblemTime()
          GhcModOutput()
      }
    } else {
      GhcModOutput()
    }
  }

  def start(): Unit = synchronized {
    HaskellSettingsState.getGhcModPath match {
      case Some(p) =>
        try {
          val command = p + " legacy-interactive"
          val process = getEnvParameters match {
            case None => Process(command, new File(project.getBasePath))
            case Some(ep) => Process(command, new File(project.getBasePath), ep.toArray: _*)
          }
          process.run(
            new ProcessIO(
              stdin => outputStream = stdin,
              stdout => Source.fromInputStream(stdout).getLines.foreach(stdOutListBuffer.+=),
              stderr => Source.fromInputStream(stderr).getLines.foreach(stdErrListBuffer.+=)
            ))
        }
        catch {
          case e: Exception =>
            HaskellNotificationGroup.notifyError("Could not start ghc-mod in interactive mode. Make sure you have set right path to ghc-mod in settings.")
            setGhcModProblemTime()
        }
      case None => {
        HaskellNotificationGroup.notifyError(s"ghc-mod could not be started in interactive mode for project ${project.getName} because ghc-mod path is not set")
      }
    }
  }

  def exit() = synchronized {
    try {
      try {
        if (outputStream != null) {
          writeToOutputStream("quit")
        }
      }
      catch {
        case e: Exception =>
          HaskellNotificationGroup.notifyError(s"Error while shutting down ghc-mod for project ${project.getName}. Error message: ${e.getMessage}")
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
      outputStream = null
    }
  }

  private def writeToOutputStream(command: String) = {
    stdOutListBuffer.clear()
    outputStream.write(command.getBytes)
    outputStream.write(LineSeparatorInBytes)
    outputStream.flush()
  }

  private def setGhcModProblemTime() = {
    ghcModProblemTime = Some(System.currentTimeMillis)
  }

  private def getEnvParameters: Option[java.util.Map[String, String]] = {
    if (OSUtil.isOSX) {
      Option(EnvironmentUtil.getEnvironmentMap)
    } else {
      None
    }
  }

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      start()
    }
  }

  override def projectClosed(): Unit = exit()

  override def initComponent(): Unit = {}

  override def disposeComponent(): Unit = {}
}

case class GhcModOutput(outputLines: Iterable[String] = Iterable())
