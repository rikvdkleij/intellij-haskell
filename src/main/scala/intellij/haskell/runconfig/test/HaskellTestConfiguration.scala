package intellij.haskell.runconfig.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

class HaskellTestConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellStackStateBase(this, environment, "test")
}
