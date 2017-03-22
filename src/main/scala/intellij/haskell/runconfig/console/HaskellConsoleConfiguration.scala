package intellij.haskell.runconfig.console

import com.intellij.execution.Executor
import com.intellij.execution.configurations._
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.runconfig.HaskellStackConfigurationBase

class HaskellConsoleConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  val myCommand = "ghci"

  override def getConfigurationEditor = new HaskellConsoleConfigurationForm(getProject)

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellConsoleState(this, environment)
}
