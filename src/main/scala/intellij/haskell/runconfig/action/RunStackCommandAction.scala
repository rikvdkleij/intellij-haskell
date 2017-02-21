package intellij.haskell.runconfig.action

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.{ExecutorRegistry, ProgramRunnerUtil, RunManagerEx, RunnerAndConfigurationSettings}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, LangDataKeys}
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.roots.ModuleRootManager
import intellij.haskell.HaskellIcons
import intellij.haskell.runconfig.HaskellStackConfigurationType

class RunStackCommandAction extends AnAction(HaskellIcons.HaskellSmallLogo) {

  override def update(event: AnActionEvent): Unit = {
    val hasStackModule = ModuleManager.getInstance(event.getProject).getModules.toList.exists(m => {
      ModuleRootManager.getInstance(m).getContentRoots.map(_.findChild("stack.yaml")).headOption.isDefined
    })

    event.getPresentation.setEnabled(hasStackModule)
    event.getPresentation.setVisible(hasStackModule)
  }

  override def actionPerformed(event: AnActionEvent): Unit = {
    getAppropriateModule(event).foreach(module => {
      val dialog = new RunStackCommandDialog(module.getProject)

      if (!dialog.showAndGet()) return

      val command :: args = dialog.getStackCommandLine
      runCommand(module, command, args.mkString(" "))
    })
  }

  private def getAppropriateModule(event: AnActionEvent): Option[Module] = {
    val stackModules = ModuleManager.getInstance(event.getProject).getModules.toList.filter(m => {
      ModuleRootManager.getInstance(m).getContentRoots.map(_.findChild("stack.yaml")).headOption.isDefined
    })
    val current = event.getData(LangDataKeys.MODULE)

    if (stackModules.contains(current)) {
      Some(current)
    } else {
      stackModules.headOption
    }
  }

  private def runCommand(module: Module, command: String, consoleArgs: String) {
    val runConfiguration = createRunConfiguration(module, command, consoleArgs)
    val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID)
    ProgramRunnerUtil.executeConfiguration(module.getProject, runConfiguration, executor)
  }

  private def createRunConfiguration(module: Module, command: String, consoleArgs: String): RunnerAndConfigurationSettings = {
    val runManager = RunManagerEx.getInstanceEx(module.getProject)

    val factories = new HaskellStackConfigurationType().getConfigurationFactories
    val newConfigurationSettings = runManager.createRunConfiguration(command, factories(3))

    val configuration = newConfigurationSettings.getConfiguration.asInstanceOf[HaskellStackCommandConfiguration]
    configuration.setCommand(command)
    configuration.setConsoleArgs(consoleArgs)

    runManager.setTemporaryConfiguration(newConfigurationSettings)
    newConfigurationSettings
  }

}
