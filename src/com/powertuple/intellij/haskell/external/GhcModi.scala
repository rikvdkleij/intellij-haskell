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
import java.util.concurrent.Executors

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.powertuple.intellij.haskell.settings.HaskellSettings

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io._
import scala.sys.process._

class GhcModi(val project: Project, val settings: HaskellSettings) extends ProjectComponent {

  private implicit val ec = new ExecutionContext {
    val threadPool = Executors.newSingleThreadExecutor

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }

  private[this] var process: Process = _
  private[this] var outputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private val OK = "OK"

  def execute(command: String): GhcModiOutput = synchronized {
    stdOutListBuffer.clear()
    stdErrListBuffer.clear()

    outputStream.write((command + "\n").getBytes)
    outputStream.flush()

    val waitForOutput = Future {
      while (stdOutListBuffer.lastOption != Some(OK)) {
        Thread.sleep(5)
      }
    }
    Await.result(waitForOutput, 2.second)

    GhcModiOutput(stdOutListBuffer.filter(_ != OK), stdErrListBuffer)
  }

  def exit() {
    outputStream.close()
  }

  def reinit() {
    exit()
    initComponent()
  }

  override def projectOpened(): Unit = {}

  override def projectClosed(): Unit = {
    exit()
  }

  override def initComponent(): Unit = {
    Process(settings.getState.ghcModiPath, new File(project.getBasePath)).run(
      new ProcessIO(
        stdin => outputStream = stdin,
        stdout => Source.fromInputStream(stdout).getLines.foreach(stdOutListBuffer.+=),
        stderr => Source.fromInputStream(stderr).getLines.foreach(stdErrListBuffer.+=)
      ))
  }

  override def disposeComponent(): Unit = {}

  override def getComponentName: String = "ghcModi"


}

case class GhcModiOutput(outputLines: Seq[String], errorLines: Seq[String])
