/*
 * Copyright 2014 Rik van der Kleij
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
import java.util.concurrent.{Executors, TimeoutException}

import com.intellij.openapi.project.Project
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.OSUtil
import sun.security.action.GetPropertyAction

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io._
import scala.sys.process._

private[external] class GhcModi(val settings: HaskellSettings, val project: Project) {

  private implicit val ec = new ExecutionContext {
    val threadPool = Executors.newSingleThreadExecutor

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable): Unit = {
      HaskellNotificationGroup.notifyError("Failure in execution context: " + t.getMessage)
    }
  }
  private final val LineSeparator = java.security.AccessController.doPrivileged(new GetPropertyAction("line.separator")).getBytes

  private[this] var outputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private val OK = "OK"

  def execute(command: String): GhcModiOutput = synchronized {
    if (outputStream == null) {
      // creating Process has failed in #startGhcModi
      return GhcModiOutput()
    }

    stdOutListBuffer.clear()
    try {
      outputStream.write(command.getBytes)
      outputStream.write(LineSeparator)
      outputStream.flush()

      val waitForStdOutput = Future {
        while (stdOutListBuffer.lastOption != Some(OK)) {
          // wait for result
        }
        stdOutListBuffer.init.toSeq
      }
      val stdOutput = Await.result(waitForStdOutput, 2.second)

      if (stdErrListBuffer.nonEmpty) {
        HaskellNotificationGroup.notifyError(s"ghc-modi error output: ${stdErrListBuffer.mkString}")
        GhcModiOutput()
      } else {
        GhcModiOutput(stdOutput)
      }
    }
    catch {
      case _: TimeoutException | _: IOException => {
        HaskellNotificationGroup.notifyError(s"Error in communication with ghc-modi. ghc-modi will be restarted. Command was: $command")
        reinit()
        GhcModiOutput()
      }
    }
  }

  def startGhcModi() {
    if (doesCabalSandboxExists) {
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
        case e: IOException => {
          HaskellNotificationGroup.notifyError("Can not get connection with ghc-modi. Make sure you have set right path to ghc-modi in settings.")
        }
      }
      HaskellNotificationGroup.notifyInfo(s"ghc-modi is started for project ${project.getName}")
    }
  }

  private def getEnvParameters: Option[(String, String)] = {
    // Workaround because of bug in Yosemite :-(
    if (OSUtil.isOSX) {
      val path = System.getenv("PATH")
      if (!path.contains("/usr/local/bin")) {
        Some(("PATH", path + ":/usr/local/bin"))
      } else {
        None
      }
    } else {
      None
    }
  }

  private def doesCabalSandboxExists = {
    new File(project.getBasePath + "/.cabal-sandbox").exists()
  }

  def reinit() {
    exit()
    startGhcModi()
  }

  def exit() {
    if (outputStream != null) {
      outputStream.close()
    }
  }
}

case class GhcModiOutput(outputLines: Seq[String] = Seq())