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

package intellij.haskell.external.commandLine

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType

import scala.concurrent.duration._

object StackCommandLine {

  private final val DefaultTimeout = 3.seconds

  private final val BuildTimeout = 30.minutes

  def runCommand(command: Seq[String], project: Project, timeoutInMillis: Long = DefaultTimeout.toMillis, captureOutputToLog: Boolean = false, logErrorAsInfo: Boolean = false): Option[ProcessOutput] = {
    HaskellSdkType.getStackPath(project).flatMap(stackPath => {
      CommandLine.runProgram(
        Some(project),
        project.getBasePath,
        stackPath,
        command,
        timeoutInMillis.toInt,
        captureOutputToLog,
        logErrorAsInfo)
    })
  }

  def executeBuild(project: Project, buildArguments: Seq[String], message: String): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, s"""Build of $message is starting with command `stack ${buildArguments.mkString(" ")}`""")
    StackCommandLine.runCommand(buildArguments, project, BuildTimeout.toMillis, captureOutputToLog = true, logErrorAsInfo = true)
    HaskellNotificationGroup.logInfoEvent(project, s"Building $message is finished")
  }
}
