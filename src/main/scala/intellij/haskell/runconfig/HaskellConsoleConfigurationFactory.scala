package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.project.Project

final class HaskellConsoleConfigurationFactory(val typez: ConfigurationType) extends ConfigurationFactory(typez) {
  override def createTemplateConfiguration(project: Project) = new HaskellConsoleConfiguration("Haskell Stack Shell", project, this)
}
