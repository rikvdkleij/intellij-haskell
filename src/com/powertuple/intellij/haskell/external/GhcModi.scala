/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.external

import java.io._
import java.util.concurrent.Executors

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.OSUtil

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io._
import scala.sys.process._

class GhcModi(val settings: HaskellSettings, val project: Project) extends ProjectComponent {

  private final val ExecutorService = Executors.newSingleThreadExecutor
  implicit private final val ExecContext = ExecutionContext.fromExecutorService(ExecutorService)

  private final val LineSeparatorInBytes = OSUtil.LineSeparator

  private[this] var outputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private val OK = "OK"

  private final val TimeOut = 5000L
  private final val GhcModiErrorIndicator = "NG"

  private var ghcModiProblemTime: Option[Long] = None

  def execute(command: String): GhcModiOutput = synchronized {
    if (ghcModiProblemTime.exists(gmpt => System.currentTimeMillis - gmpt < TimeOut)) {
      return GhcModiOutput()
    }

    if (outputStream == null) {
      startGhcModi()
    }

    if (outputStream != null) {
      try {
        stdOutListBuffer.clear()
        outputStream.write(command.getBytes)
        outputStream.write(LineSeparatorInBytes)
        outputStream.flush()

        val waitForStdOutput = Future {
          while (stdOutListBuffer.lastOption != Some(OK) && !stdOutListBuffer.headOption.exists(_.startsWith(GhcModiErrorIndicator))) {
            // wait for result
          }
          stdOutListBuffer.toIterable
        }
        val stdOutput = Await.result(waitForStdOutput, 5.second)

        if (stdErrListBuffer.nonEmpty) {
          HaskellNotificationGroup.notifyError(s"ghc-modi error output: ${stdErrListBuffer.mkString}")
          GhcModiOutput()
        } else if (stdOutput.headOption.exists(_.startsWith(GhcModiErrorIndicator))) {
          HaskellNotificationGroup.notifyError(s"ghc-modi error output: ${stdOutput.mkString(" ")}")
          GhcModiOutput()
        } else {
          GhcModiOutput(stdOutput.init)
        }
      }
      catch {
        case e: Exception =>
          HaskellNotificationGroup.notifyError(s"Error in communication with ghc-modi: ${e.getMessage}. Check if GHC SDK is set and ghc-modi is okay. ghc-modi will not be called for 5 seconds. Command was: $command")
          setGhcModiProblemTime()
          exit()
          GhcModiOutput()
      }
    } else {
      GhcModiOutput()
    }
  }

  private def startGhcModi() {
    if (!settings.getState.ghcModiPath.isEmpty) {
      try {
        val process = getEnvParameters match {
          case None => Process(settings.getState.ghcModiPath, new File(project.getBasePath))
          case Some(ep) => Process(settings.getState.ghcModiPath, new File(project.getBasePath), ep)
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
          HaskellNotificationGroup.notifyError("Can not start ghc-modi. Make sure you have set right path to ghc-modi in settings.")
          setGhcModiProblemTime()
          return
      }
      HaskellNotificationGroup.notifyInfo(s"ghc-modi is called to startup for project ${project.getName}")
    } else {
      HaskellNotificationGroup.notifyError(s"ghc-modi is not started for project ${project.getName} because ghc-modi path is not defined")
      outputStream = null
    }
  }

  private def setGhcModiProblemTime() = {
    ghcModiProblemTime = Some(System.currentTimeMillis)
  }

  private def getEnvParameters: Option[(String, String)] = {
    // Workaround because of bug in Yosemite :-(
    if (OSUtil.isOSX) {
      for {
        pm <- Option(ProjectRootManager.getInstance(project))
        ps <- Option(pm.getProjectSdk)
        ghcDir <- Option(ps.getHomePath)
        pathEnv = System.getenv("PATH")
      } yield ("PATH", pathEnv + ":" + ghcDir)
    } else {
      None
    }
  }

  def exit() {
    try {
      execute("quit")
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

  override def projectOpened(): Unit = {}

  override def projectClosed(): Unit = {
    exit()
  }

  override def initComponent(): Unit = {}

  override def disposeComponent(): Unit = {}

  override def getComponentName: String = "ghc-modi"
}

case class GhcModiOutput(outputLines: Iterable[String] = Iterable())