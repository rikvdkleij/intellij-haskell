package intellij.haskell.runconfig.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.runconfig.HaskellStackConfigurationBase

class HaskellRunConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  val myCommand = s"build --exec ${project.getName}"

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellRunningState(this, environment)
}
