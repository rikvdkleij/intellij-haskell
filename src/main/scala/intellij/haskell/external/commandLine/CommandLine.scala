/*
 * Copyright 2016 Rik van der Kleij

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

package intellij.haskell.external.commandLine

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType
import com.intellij.execution.process._
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import intellij.haskell.HaskellNotificationGroup

import scala.collection.JavaConverters._

object CommandLine {
  private final val StandardTimeoutInMillis = 1000

  def runProgram(project: Option[Project], workDir: String, commandPath: String, arguments: Seq[String], timeoutInMillis: Long = StandardTimeoutInMillis, captureOutputToLog: Boolean = false, logErrorAsInfo: Boolean = false): Option[ProcessOutput] = {
    val commandLine = new GeneralCommandLine
    commandLine.withWorkDirectory(workDir)
    commandLine.setExePath(commandPath)
    commandLine.addParameters(arguments.asJava)
    commandLine.withParentEnvironmentType(ParentEnvironmentType.CONSOLE)
    run(project, commandLine, timeoutInMillis, captureOutputToLog, logErrorAsInfo)
  }

  private def run(project: Option[Project], cmd: GeneralCommandLine, timeout: Long, captureOutputToLog: Boolean, logErrorAsInfo: Boolean): Option[ProcessOutput] = {
    val processHandler = if (captureOutputToLog) {
      new CapturingProcessHandler(cmd) {
        override protected def createProcessAdapter(processOutput: ProcessOutput): CapturingProcessAdapter = new CapturingProcessToLog(project, cmd, processOutput, logErrorAsInfo)
      }
    } else {
      new CapturingProcessHandler(cmd)
    }

    val processOutput = processHandler.runProcess(timeout.toInt, true)
    if (processOutput.isTimeout) {
      HaskellNotificationGroup.logErrorBalloonEvent(project, s"Timeout while executing <b>${cmd.getCommandLineString}</b>.")
      None
    } else if (!captureOutputToLog && !processOutput.getStderrLines.isEmpty) {
      HaskellNotificationGroup.logErrorEvent(project, s"Error while executing `${cmd.getCommandLineString}`:  ${processOutput.getStderr}")
      Option(processOutput)
    } else {
      Option(processOutput)
    }
  }

  private class CapturingProcessToLog(val project: Option[Project], val cmd: GeneralCommandLine, val output: ProcessOutput, val logErrorAsInfo: Boolean) extends CapturingProcessAdapter {

    override def onTextAvailable(event: ProcessEvent, outputType: Key[_]) {
      addOutput(event.getText, outputType)
    }

    private def addOutput(text: String, outputType: Key[_]) {
      if (!text.trim.isEmpty) {
        if (outputType == ProcessOutputTypes.STDERR && !logErrorAsInfo) {
          HaskellNotificationGroup.logErrorEvent(project, s"Error while executing `${cmd.getCommandLineString}`:  $text")
        } else {
          HaskellNotificationGroup.logInfoEvent(project, s"Info while executing `${cmd.getCommandLineString}`:  $text")
        }
      }
    }
  }

}
