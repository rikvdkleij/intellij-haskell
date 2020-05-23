package intellij.haskell.runconfig.test

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.project.Project

class HaskellTestConfigurationFactory(val typez: ConfigurationType) extends ConfigurationFactory(typez) {
  private val name = "Haskell Stack Tester"

  override def createTemplateConfiguration(project: Project) = new HaskellTestConfiguration(name, project, this)

  override def getName: String = name

  override def getId: String = name
}
