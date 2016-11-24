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

import java.io.File

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType
import com.intellij.execution.process._
import com.intellij.openapi.util.Key
import intellij.haskell.HaskellNotificationGroup

import scala.collection.JavaConverters._

object CommandLine {
  private final val StandardTimeoutInMillis = 1000

  def runCommand(workDir: String, commandPath: String, arguments: Seq[String], timeoutInMillis: Long = StandardTimeoutInMillis, captureOutputToLog: Boolean = false): Option[ProcessOutput] = {
    if (!new File(workDir).isDirectory || !new File(commandPath).canExecute) {
      new ProcessOutput
    }
    val cmd = new GeneralCommandLine
    cmd.withWorkDirectory(workDir)
    cmd.setExePath(commandPath)
    cmd.addParameters(arguments.asJava)
    cmd.withParentEnvironmentType(ParentEnvironmentType.CONSOLE)
    execute(cmd, timeoutInMillis, captureOutputToLog)
  }

  private def execute(cmd: GeneralCommandLine, timeout: Long, captureOutputToLog: Boolean): Option[ProcessOutput] = {
    val processHandler = if (captureOutputToLog) {
      new CapturingProcessHandler(cmd) {
        override protected def createProcessAdapter(processOutput: ProcessOutput): CapturingProcessAdapter = new CapturingProcessToLog(cmd, processOutput)
      }
    } else {
      new CapturingProcessHandler(cmd)
    }

    val processOutput = processHandler.runProcess(timeout.toInt, true)
    if (processOutput.isTimeout) {
      HaskellNotificationGroup.notifyBalloonWarning(s"Timeout while `${cmd.getCommandLineString}`")
      None
    } else if (!captureOutputToLog && !processOutput.getStderrLines.isEmpty) {
      processOutput.getStderrLines.asScala.foreach(line => HaskellNotificationGroup.logWarning(s"${cmd.getCommandLineString}  -  $line"))
      Option(processOutput)
    } else {
      Option(processOutput)
    }
  }

  private class CapturingProcessToLog(val cmd: GeneralCommandLine, val output: ProcessOutput) extends CapturingProcessAdapter {

    override def onTextAvailable(event: ProcessEvent, outputType: Key[_]) {
      addOutput(event.getText, outputType)
    }

    private def addOutput(text: String, outputType: Key[_]) {
      if (!text.trim.isEmpty) {
        if (outputType == ProcessOutputTypes.STDERR) {
          HaskellNotificationGroup.logWarning(s"${cmd.getCommandLineString}  -  $text")
        } else {
          HaskellNotificationGroup.logInfo(s"${cmd.getCommandLineString}  -  $text")
        }
      }
    }
  }

}
