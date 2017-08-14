package intellij.haskell.runconfig.run

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.project.Project

class HaskellRunConfigurationFactory(val typez: ConfigurationType) extends ConfigurationFactory(typez) {
  private val name = "Haskell Stack Runner"

  override def createTemplateConfiguration(project: Project) = new HaskellRunConfiguration(name, project, this)

  override def getName: String = name
}
