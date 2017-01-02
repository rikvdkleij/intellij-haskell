package intellij.haskell.repl

import java.io.File
import java.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.console.{ConsoleHistoryController, ProcessBackedConsoleExecuteActionHandler}
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory
import com.intellij.execution.{CantRunException, ExecutionException, ExecutionHelper}
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.{ModuleRootManager, ProjectRootManager}
import intellij.haskell.sdk.HaskellSdkType

object HaskellConsoleRunner {
  private[repl] val REPLTitle = "GHCi"

  def run(module: Module): HaskellConsoleProcessHandler = {
    val srcRoot = ModuleRootManager.getInstance(module).getContentRoots()(0).getPath
    val path = srcRoot + File.separator + "src"
    val runner = new HaskellConsoleRunner(module, REPLTitle, path)
    try {
      runner.initAndRun()
      runner.getProcessHandler.asInstanceOf[HaskellConsoleProcessHandler]
    } catch {
      case e: ExecutionException =>
        ExecutionHelper.showErrors(module.getProject, util.Arrays.asList[Exception](e), REPLTitle, null)
        null
    }
  }

  @throws[CantRunException]
  private def createCommandLine(module: Module, workingDir: String) = {
    val sdk = ProjectRootManager.getInstance(module.getProject).getProjectSdk
    if (sdk == null || !sdk.getSdkType.isInstanceOf[HaskellSdkType] || sdk.getHomePath == null) throw new CantRunException("Invalid SDK Home path set. Please set your SDK path correctly.")
    new GeneralCommandLine(sdk.getHomePath).withParameters("ghci").withWorkDirectory(workingDir)
  }
}

final class HaskellConsoleRunner private(val module: Module, val consoleTitle: String, val workingDir: String)
  extends AbstractConsoleRunnerWithHistory[HaskellConsole](module.getProject, consoleTitle, workingDir) {
  private val project = module.getProject
  private val myType = "haskell"
  private var cmdline: GeneralCommandLine = _

  protected def createExecuteActionHandler: ProcessBackedConsoleExecuteActionHandler = {
    new ConsoleHistoryController(myType, "", getConsoleView).install()
    new ProcessBackedConsoleExecuteActionHandler(getProcessHandler, false)
  }

  protected def createConsoleView = new HaskellConsole(project, consoleTitle)

  @throws[ExecutionException]
  protected def createProcess: Process = {
    cmdline = HaskellConsoleRunner.createCommandLine(module, workingDir)
    cmdline.createProcess
  }

  protected def createProcessHandler(process: Process) = new HaskellConsoleProcessHandler(process, cmdline.getCommandLineString, getConsoleView)
}
