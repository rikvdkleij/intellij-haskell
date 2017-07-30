/*
 * Copyright 2014-2017 Rik van der Kleij
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

package intellij.haskell.external.execution

import java.io.File

import com.intellij.compiler.impl.{CompileDriver, ProjectCompileScope}
import com.intellij.execution.ExecutionException
import com.intellij.execution.process._
import com.intellij.openapi.application.{Result, WriteAction}
import com.intellij.openapi.compiler.{CompileContext, CompileTask, CompilerMessageCategory}
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.{CharsetToolkit, VfsUtil}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.mutable.ListBuffer

object StackCommandLine {

  def runCommand(project: Project, command: Seq[String], timeoutInMillis: Long = CommandLine.DefaultTimeout.toMillis, captureOutput: Option[CaptureOutput] = None, notifyBalloonError: Boolean = false): Option[ProcessOutput] = {
    HaskellSdkType.getStackPath(project).flatMap(stackPath => {
      CommandLine.runProgram(
        Some(project),
        project.getBasePath,
        stackPath,
        command,
        timeoutInMillis.toInt,
        captureOutput,
        notifyBalloonError)
    })
  }

  def executeBuild(project: Project, buildArguments: Seq[String], message: String, notifyBalloonError: Boolean = false): Option[ProcessOutput] = {
    logStart(project, buildArguments)
    val processOutput = runCommand(project, Seq("build") ++ buildArguments, -1, Some(CaptureOutputToLog))
    if (processOutput.isEmpty || processOutput.exists(_.getExitCode != 0)) {
      HaskellNotificationGroup.logErrorEvent(project, s"Building `$message` has failed")
    } else {
      HaskellNotificationGroup.logInfoEvent(project, s"Building `$message` is finished successfully")
    }
    processOutput
  }


  def executeInMessageView(project: Project, arguments: Seq[String], progressIndicator: ProgressIndicator): Option[Boolean] = HaskellSdkType.getStackPath(project).flatMap(stackPath => {
    logStart(project, arguments)
    val cmd = CommandLine.createCommandLine(project.getBasePath, stackPath, arguments)
    (try {
      Option(cmd.createProcess())
    } catch {
      case e: ExecutionException =>
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while starting `${cmd.getCommandLineString}`: ${e.getMessage}")
        None
    }).map(process => {

      val handler = new BaseOSProcessHandler(process, cmd.getCommandLineString, CharsetToolkit.getDefaultSystemCharset)

      val task = new CompileTask {

        def execute(compileContext: CompileContext): Boolean = {
          val adapter = new MessageViewProcessAdapter(compileContext, progressIndicator)
          handler.addProcessListener(adapter)
          handler.startNotify()
          handler.waitFor()
          adapter.addLastMessage()
          handler.getExitCode == 0
        }
      }

      val compileDriver = new CompileDriver(project)

      new WriteAction[Unit]() {

        override def run(result: Result[Unit]): Unit = {
          compileDriver.executeCompileTask(task, new ProjectCompileScope(project), s"executing ${cmd.getCommandLineString}", null)
        }
      }.execute()

      handler.waitFor()
      handler.getExitCode == 0
    })
  })

  private def logStart(project: Project, buildArguments: Seq[String]) = {
    HaskellNotificationGroup.logInfoEvent(project, s"""Starting to execute `stack ${buildArguments.mkString(" ")}`""")
  }

  private class MessageViewProcessAdapter(val compileContext: CompileContext, val progressIndicator: ProgressIndicator) extends ProcessAdapter() {

    private val previousMessageLines = ListBuffer[String]()

    override def onTextAvailable(event: ProcessEvent, outputType: Key[_]) {
      val text = event.getText
      progressIndicator.setText(text)
      addToMessageView(text, outputType)
    }

    def addLastMessage(): Unit = {
      if (previousMessageLines.nonEmpty) {
        addMessage()
      }
    }

    private def addToMessageView(text: String, outputType: Key[_]) {
      if (text.trim.nonEmpty) {
        if (outputType == ProcessOutputTypes.STDERR) {
          if (previousMessageLines.nonEmpty && !text.startsWith("  ")) {
            addMessage()
          }
          previousMessageLines.append(text)
        } else if (text.startsWith("Warning:")) {
          compileContext.addMessage(CompilerMessageCategory.WARNING, text, null, -1, -1)
        } else {
          compileContext.addMessage(CompilerMessageCategory.INFORMATION, text, null, -1, -1)
        }
      }
    }

    private def addMessage() = {
      val errorMessageLine = previousMessageLines.mkString(" ")
      val compilationProblem = HaskellCompilationResultHelper.parseErrorLine(None, errorMessageLine.replaceAll("\n", " "))
      compilationProblem match {
        case Some(p@CompilationProblemInOtherFile(filePath, lineNr, columnNr, message)) if p.isWarning =>
          compileContext.addMessage(CompilerMessageCategory.WARNING, message, getFileUrl(filePath), lineNr, columnNr)
        case Some(CompilationProblemInOtherFile(filePath, lineNr, columnNr, message)) =>
          compileContext.addMessage(CompilerMessageCategory.ERROR, message, getFileUrl(filePath), lineNr, columnNr)
        case _ =>
          val compilerMessageCategory =
            if (errorMessageLine.contains("ExitFailure")) {
              CompilerMessageCategory.ERROR
            } else if (errorMessageLine.startsWith("Warning:")) {
              CompilerMessageCategory.WARNING
            } else {
              CompilerMessageCategory.INFORMATION
            }
          compileContext.addMessage(compilerMessageCategory, errorMessageLine, null, -1, -1)
      }
      previousMessageLines.clear()
    }

    private def getFileUrl(filePath: String) = {
      VfsUtil.getUrlForLibraryRoot(new File(filePath))
    }
  }

}
