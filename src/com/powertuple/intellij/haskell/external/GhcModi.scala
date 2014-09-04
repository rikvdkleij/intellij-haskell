/*
 * Copyright 2014 Rik van der Kleij

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

    def reportFailure(t: Throwable) {}
  }

  private[this] var outputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private val OK = "OK"

  def execute(command: String): GhcModOutput = synchronized {
    if (outputStream == null) {
      // creating Process has failed in #startGhcModi
      return GhcModOutput()
    }

    stdOutListBuffer.clear()
    try {
      outputStream.write((command + "\n").getBytes)
      outputStream.flush()

      val waitForOutput = Future {
        while (stdOutListBuffer.lastOption != Some(OK)) {
          Thread.sleep(5)
        }
      }
      Await.result(waitForOutput, 2.second)

      if (stdErrListBuffer.nonEmpty) {
        HaskellNotificationGroup.notifyError(s"ghc-modi error output: ${stdErrListBuffer.mkString}")
        GhcModOutput()
      } else {
        GhcModOutput(stdOutListBuffer.filter(_ != OK))
      }
    }
    catch {
      case _: TimeoutException | _: IOException => {
        HaskellNotificationGroup.notifyError(s"Error in communication with ghc-modi. ghc-modi will be restarted. Command was: $command")
        reinit()
        GhcModOutput()
      }
    }
  }

  def startGhcModi() {
    try {
      Process(settings.getState.ghcModiPath, new File(project.getBasePath)).run(
        new ProcessIO(
          stdin => outputStream = stdin,
          stdout => Source.fromInputStream(stdout).getLines.foreach(stdOutListBuffer.+=),
          stderr => Source.fromInputStream(stderr).getLines.foreach(stdErrListBuffer.+=)
        ))
    }
    catch {
      case e: IOException => {
        HaskellNotificationGroup.notifyError("Can not get connection with ghc-modi. Make sure you have set right path in settings to ghc-modi.")
      }
    }
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

case class GhcModOutput(outputLines: Seq[String] = Seq())