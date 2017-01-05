package intellij.haskell.repl

import java.io.File
import java.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.console.{ConsoleHistoryController, ConsoleRootType, ProcessBackedConsoleExecuteActionHandler}
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory
import com.intellij.execution.{CantRunException, ExecutionException, ExecutionHelper}
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.{ModuleRootManager, ProjectRootManager}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.sdk.HaskellSdkType

object HaskellConsoleRunner {
  val REPLTitle = "λ "

  def run(module: Module, fileName: String): Option[HaskellConsoleProcessHandler] = {
    ModuleRootManager.getInstance(module).getContentRoots.headOption.flatMap(root => {
      val srcRoot = root.getPath
      val path = srcRoot + File.separator + "src"
      val runner = new HaskellConsoleRunner(module, REPLTitle + fileName, path)
      try {
        runner.initAndRun()
        Some(runner.getProcessHandler.asInstanceOf[HaskellConsoleProcessHandler])
      } catch {
        case e: ExecutionException =>
          ExecutionHelper.showErrors(module.getProject, util.Arrays.asList[Exception](e), REPLTitle, null)
          None
      }
    })
  }

  private def createCommandLine(module: Module, workingDir: String) = {
    val sdk = ProjectRootManager.getInstance(module.getProject).getProjectSdk
    if (sdk == null || !sdk.getSdkType.isInstanceOf[HaskellSdkType] || sdk.getHomePath == null) {
      HaskellNotificationGroup.logErrorBalloonEvent(module.getProject, "Please make sure your <b>Stack SDK</b> is configured correctly.")
      throw new CantRunException("Invalid Stack SDK.")
    } else {
      new GeneralCommandLine(sdk.getHomePath)
        .withParameters("ghci")
        .withWorkDirectory(workingDir)
    }
  }
}

final class HaskellConsoleRunner private(val module: Module, val consoleTitle: String, val workingDir: String)
  extends AbstractConsoleRunnerWithHistory[HaskellConsole](module.getProject, consoleTitle, workingDir) {
  private val project = module.getProject
  private val myType = new ConsoleRootType("haskell", "Haskell") {}
  private var cmdline: GeneralCommandLine = _

  protected def createExecuteActionHandler: ProcessBackedConsoleExecuteActionHandler = {
    new ConsoleHistoryController(myType, "haskell", getConsoleView).install()
    new ProcessBackedConsoleExecuteActionHandler(getProcessHandler, false)
  }

  protected def createConsoleView = new HaskellConsole(project, consoleTitle)

  protected def createProcess: Process = {
    cmdline = HaskellConsoleRunner.createCommandLine(module, workingDir)
    cmdline.createProcess
  }

  protected def createProcessHandler(process: Process) = new HaskellConsoleProcessHandler(process, cmdline.getCommandLineString, getConsoleView)
}
