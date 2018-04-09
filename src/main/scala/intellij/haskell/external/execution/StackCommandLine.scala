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

import com.intellij.compiler.impl._
import com.intellij.compiler.progress.CompilerTask
import com.intellij.execution.ExecutionException
import com.intellij.execution.process._
import com.intellij.openapi.compiler.{CompileContext, CompileTask, CompilerMessageCategory}
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.CharsetToolkit
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.{GhcVersion, HaskellFileUtil, HaskellProjectUtil}

import scala.collection.mutable.ListBuffer
import scala.concurrent.SyncVar

object StackCommandLine {

  def run(project: Project, arguments: Seq[String], timeoutInMillis: Long = CommandLine.DefaultTimeout.toMillis,
          ignoreExitCode: Boolean = false): Option[ProcessOutput] = {
    HaskellSdkType.getStackPath(project).map(stackPath => {
      CommandLine.run(
        Some(project),
        project.getBasePath,
        stackPath,
        arguments,
        timeoutInMillis.toInt,
        ignoreExitCode = ignoreExitCode
      )
    })
  }

  def build(project: Project, buildTarget: String, logBuildResult: Boolean, fast: Boolean = false): Option[ProcessOutput] = {
    val arguments = Seq("build", buildTarget) ++ (if (fast) Seq("--fast") else Seq())
    val processOutput = run(project, arguments, -1, ignoreExitCode = true)
    if (logBuildResult) {
      if (processOutput.isEmpty || processOutput.exists(_.getExitCode != 0)) {
        HaskellNotificationGroup.logErrorEvent(project, s"Building `$buildTarget` has failed, see Haskell Event log for more information")
      } else {
        HaskellNotificationGroup.logInfoEvent(project, s"Building `$buildTarget` is finished successfully")
      }
    }
    processOutput
  }

  def build(project: Project, buildTarget: String, logBuildResult: Boolean, fast: Boolean = false): Option[ProcessOutput] = {
    val arguments = Seq("build", buildTarget) ++ (if (fast) Seq("--fast") else Seq())
    val processOutput = run(project, arguments, -1, ignoreExitCode = true)
    if (logBuildResult) {
      if (processOutput.isEmpty || processOutput.exists(_.getExitCode != 0)) {
        HaskellNotificationGroup.logErrorEvent(project, s"Building `$buildTarget` has failed, see Haskell Event log for more information")
      } else {
        HaskellNotificationGroup.logInfoEvent(project, s"Building `$buildTarget` is finished successfully")
      }
    }
    processOutput
  }

  private def ghcOptions(project: Project) = {
    HaskellProjectUtil.getGhcVersion(project).map(
      ghcVersion =>
        if (ghcVersion >= GhcVersion(8, 2, 1)) {
          Seq("--ghc-options", NoDiagnosticsShowCaretFlag)
        } else {
          Seq()
        }
    ).getOrElse(Seq())
  }

  def buildProjectDependenciesInMessageView(project: Project): Option[Boolean] = {
    StackCommandLine.executeInMessageView(project, Seq("build", "--fast", "--test", "--bench", "--no-run-tests", "--no-run-benchmarks", "--only-dependencies") ++ ghcOptions(project))
  }

  def buildProjectInMessageView(project: Project): Option[Boolean] = {
    StackCommandLine.executeInMessageView(project, Seq("build", "--fast") ++ ghcOptions(project))
  }

  def executeInMessageView(project: Project, arguments: Seq[String]): Option[Boolean] = {
    HaskellSdkType.getStackPath(project).flatMap(stackPath => {
      val cmd = CommandLine.createCommandLine(project.getBasePath, stackPath, arguments)
      (try {
        Option(cmd.createProcess())
      } catch {
        case e: ExecutionException =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while starting `${cmd.getCommandLineString}`: ${e.getMessage}")
          None
      }).map(process => {

        val handler = new BaseOSProcessHandler(process, cmd.getCommandLineString, CharsetToolkit.getDefaultSystemCharset)

        val compilerTask = new CompilerTask(project, s"executing ${cmd.getCommandLineString}", false, false, true, true)
        val compileTask = new CompileTask {

          def execute(compileContext: CompileContext): Boolean = {
            val adapter = new MessageViewProcessAdapter(compileContext, compilerTask.getIndicator)
            handler.addProcessListener(adapter)
            handler.startNotify()
            handler.waitFor()
            adapter.addLastMessage()
            handler.getExitCode == 0
          }
        }

        val compileContext = new CompileContextImpl(project, compilerTask, new ProjectCompileScope(project), false, false)

        val compileResult = new SyncVar[Boolean]

        compilerTask.start(() => {
          compileResult.put(compileTask.execute(compileContext))
        }, null)

        compileResult.get
      })
    })

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
          compileContext.addMessage(CompilerMessageCategory.WARNING, message, HaskellFileUtil.getUrlByPath(filePath), lineNr, columnNr)
        case Some(CompilationProblemInOtherFile(filePath, lineNr, columnNr, message)) =>
          compileContext.addMessage(CompilerMessageCategory.ERROR, message, HaskellFileUtil.getUrlByPath(filePath), lineNr, columnNr)
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
  }

}
