package intellij.haskell.runconfig.action

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

class HaskellStackCommandConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var myCommand: String = ""

  def setCommand(consoleArgs: String) {
    myCommand = consoleArgs
  }

  def getCommand: String = {
    myCommand
  }

  override def getConfigurationEditor = new HaskellStackCommandConfigurationForm(getProject)

  override def getState(executor: Executor, environment: ExecutionEnvironment) =
    new HaskellStackStateBase(this, environment, getCommand.split(" ").toList)
}
