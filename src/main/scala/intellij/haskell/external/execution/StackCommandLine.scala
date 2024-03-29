/*
 * Copyright 2014-2020 Rik van der Kleij
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
import com.intellij.concurrency.JobSchedulerImpl.getCPUCoresCount
import com.intellij.execution.ExecutionException
import com.intellij.execution.process._
import com.intellij.openapi.compiler.{CompileContext, CompileTask, CompilerMessageCategory}
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.{CharsetToolkit, VfsUtil}
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

import java.util.concurrent.{LinkedBlockingDeque, TimeUnit}
import scala.jdk.CollectionConverters._

object StackCommandLine {

  final val NoDiagnosticsShowCaretFlag = "-fno-diagnostics-show-caret"

  def stackVersion(project: Project): Option[String] = {
    StackCommandLine.run(project, Seq("--numeric-version"), enableExtraArguments = false).flatMap(_.getStdoutLines.asScala.headOption)
  }

  def run(project: Project, arguments: Seq[String], timeoutInMillis: Long = CommandLine.DefaultTimeout.toMillis,
          ignoreExitCode: Boolean = false, logOutput: Boolean = false, workDir: Option[String] = None, notifyBalloonError: Boolean = false, enableExtraArguments: Boolean = true): Option[ProcessOutput] = {
    HaskellSdkType.getStackPath(project).map(stackPath => {
      CommandLine.runInWorkDir(
        project,
        workDir.getOrElse(project.getBasePath),
        stackPath,
        arguments ++ (if (enableExtraArguments) HaskellSettingsState.getExtraStackArguments else Seq()),
        timeoutInMillis.toInt,
        ignoreExitCode = ignoreExitCode,
        logOutput = logOutput,
        notifyBalloonError = notifyBalloonError
      )
    })
  }

  def runWithProgressIndicator(project: Project, workDir: Option[String], arguments: Seq[String], progressIndicator: Option[ProgressIndicator]): Option[CapturingProcessHandler] = {
    HaskellSdkType.getStackPath(project).map(stackPath => {
      CommandLine.runWithProgressIndicator(
        project,
        workDir,
        stackPath,
        arguments,
        progressIndicator
      )
    })
  }

  def installTool(project: Project, progressIndicator: ProgressIndicator, toolName: String): Boolean = {
    import intellij.haskell.GlobalInfo._
    val systemGhcOption = if (StackYamlComponent.isNixEnabled(project) || !HaskellSettingsState.useSystemGhc) {
      Seq()
    } else {
      Seq("--system-ghc")
    }

    val cpuCoresCount = getCPUCoresCount
    val jobsCount = if (cpuCoresCount > 2) (cpuCoresCount / 2) + 1 else 1
    val arguments = systemGhcOption ++ Seq(s"-j$jobsCount", "--stack-root", toolsStackRootPath.getPath, "--resolver", StackageLtsVersion, "--local-bin-path", toolsBinPath.getPath, "install", toolName)

    val result = runWithProgressIndicator(project, workDir = Some(VfsUtil.getUserHomeDir.getPath), arguments, Some(progressIndicator)).exists(handler => {
      val output = handler.runProcessWithProgressIndicator(progressIndicator)

      if (output.isCancelled) {
        handler.destroyProcess()
      }

      if (output.getExitCode != 0) {
        if (output.getStderr.nonEmpty) {
          HaskellNotificationGroup.logErrorBalloonEvent(project, output.getStderr)
        }
        if (output.getStdout.nonEmpty) {
          HaskellNotificationGroup.logErrorBalloonEvent(project, output.getStdout)
        }
      }
      output.getExitCode == 0 && !output.isCancelled && !output.isTimeout
    })

    result
  }

  def updateStackIndex(project: Project): Option[ProcessOutput] = {
    val arguments = Seq("update")
    run(project, arguments, -1, logOutput = true, notifyBalloonError = true, enableExtraArguments = false)
  }

  def buildProjectDependenciesInMessageView(project: Project): Option[Boolean] = {
    buildInMessageView(project, "Build project dependencies", Seq("--test", "--bench", "--no-run-tests", "--no-run-benchmarks", "--only-dependencies"))
  }

  private def ghcOptions(project: Project) = {
    if (HaskellProjectUtil.setNoDiagnosticsShowCaretFlag(project)) {
      Seq("--ghc-options", NoDiagnosticsShowCaretFlag)
    } else {
      Seq()
    }
  }

  def buildInBackground(project: Project, arguments: Seq[String]): Option[Boolean] = {
    run(project, Seq("build", "--fast") ++ arguments).map(_.getExitCode == 0)
  }

  def buildInMessageView(project: Project, description: String, arguments: Seq[String]): Option[Boolean] = {
    executeStackCommandInMessageView(project, description, Seq("build", "--fast", "--no-interleaved-output") ++ arguments ++ ghcOptions(project))
  }

  def executeStackCommandInMessageView(project: Project, description: String, arguments: Seq[String]): Option[Boolean] = {
    HaskellSdkType.getStackPath(project).flatMap(stackPath => {
      executeInMessageView(project, description, stackPath, arguments)
    })
  }

  // To prevent message window is not yet available
  private def waitForProjectIsInitialized(project: Project): WaitFor = {
    new WaitFor(5000, 1) {
      override def condition(): Boolean = {
        project.isInitialized
      }
    }
  }

  def executeInMessageView(project: Project, description: String, commandPath: String, arguments: Seq[String]): Option[Boolean] = {
    waitForProjectIsInitialized(project)

    val cmd = CommandLine.createCommandLine(project.getBasePath, commandPath, arguments ++ HaskellSettingsState.getExtraStackArguments)
    (try {
      Option(cmd.createProcess())
    } catch {
      case e: ExecutionException =>
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while starting `${cmd.getCommandLineString}`: ${e.getMessage}")
        None
    }).map(process => {

      val handler = new BaseOSProcessHandler(process, cmd.getCommandLineString, CharsetToolkit.getDefaultSystemCharset)

      val compilerTask = new CompilerTask(project, description, false, false, true, true)
      val compileTask = new CompileTask {

        def execute(compileContext: CompileContext): Boolean = {
          val adapter = new MessageViewProcessAdapter(compileContext)
          handler.addProcessListener(adapter)
          handler.startNotify()
          handler.waitFor(30 * 60 + 1000) // Wait max half an hour
          adapter.addLastMessage()
          handler.getExitCode == 0 || handler.getExitCode == null
        }
      }

      val compileContext = new CompileContextImpl(project, compilerTask, new ProjectCompileScope(project), false, false)

      val compileResult = new LinkedBlockingDeque[Boolean](1)

      compilerTask.start(() => {
        compileResult.put(compileTask.execute(compileContext))
      }, null)

      val result = compileResult.poll(30, TimeUnit.MINUTES) // Wait max half an hour
      val exitStatus = if (result) ExitStatus.SUCCESS else ExitStatus.ERRORS
      compilerTask.setEndCompilationStamp(exitStatus, System.currentTimeMillis)
      result
    })
  }


  private class MessageViewProcessAdapter(val compileContext: CompileContext) extends ProcessAdapter() {

    private val ansiEscapeDecoder = new AnsiEscapeDecoder()
    private val previousMessageLines = new LinkedBlockingDeque[String]
    @volatile
    private var globalError = false

    override def onTextAvailable(event: ProcessEvent, outputType: Key[_]): Unit = {
      val text = AnsiDecoder.decodeAnsiCommandsToString(event.getText, outputType, ansiEscapeDecoder)
      addToMessageView(text, outputType)
    }

    def addLastMessage(): Unit = {
      if (!previousMessageLines.isEmpty) {
        addMessage()
      }
    }

    private def addToMessageView(text: String, outputType: Key[_]): Unit = {
      if (text.trim.nonEmpty) {
        if (outputType == ProcessOutputTypes.STDERR) {
          if (text.startsWith("Error:") && text.trim.endsWith(":") || text.startsWith("Unable to parse") || text.startsWith("Error parsing")) {
            globalError = true // To get also all lines after this line indicated as error AND in order
          }

          // End of sentence which was over multiple lines
          if (!previousMessageLines.isEmpty && !text.startsWith("  ")) {
            addMessage()
          }

          previousMessageLines.add(text)
        } else if (text.startsWith("Warning:")) {
          compileContext.addMessage(CompilerMessageCategory.WARNING, text, null, -1, -1)
        } else if (text.startsWith("Error:")) {
          compileContext.addMessage(CompilerMessageCategory.ERROR, text, null, -1, -1)
        } else {
          compileContext.addMessage(CompilerMessageCategory.INFORMATION, text, null, -1, -1)
        }
      }
    }


    private def addMessage(): Unit = {
      val errorMessageLine = previousMessageLines.iterator().asScala.mkString(" ")
      val compilationProblem = HaskellCompilationResultHelper.parseErrorLine(errorMessageLine.replaceAll("\n", " "))
      compilationProblem match {
        case Some(p@CompilationProblem(filePath, lineNr, columnNr, message)) if p.isWarning =>
          compileContext.addMessage(CompilerMessageCategory.WARNING, message, HaskellFileUtil.getUrlByPath(filePath), lineNr, columnNr)
        case Some(CompilationProblem(filePath, lineNr, columnNr, message)) =>
          compileContext.addMessage(CompilerMessageCategory.ERROR, message, HaskellFileUtil.getUrlByPath(filePath), lineNr, columnNr)
        case _ =>
          val compilerMessageCategory =
            if (globalError || errorMessageLine.contains("ExitFailure") || errorMessageLine.startsWith("Error:")) {
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
