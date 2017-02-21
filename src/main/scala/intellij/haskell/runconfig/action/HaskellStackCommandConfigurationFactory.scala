package intellij.haskell.runconfig.action

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.project.Project

class HaskellStackCommandConfigurationFactory(val typez: ConfigurationType) extends ConfigurationFactory(typez) {
  private val name = "Haskell Stack Command"

  override def createTemplateConfiguration(project: Project) = new HaskellStackCommandConfiguration(name, project, this)

  override def getName: String = name
}
