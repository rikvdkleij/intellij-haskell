package intellij.haskell.runconfig.console

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.project.Project

class HaskellConsoleConfigurationFactory(val typez: ConfigurationType) extends ConfigurationFactory(typez) {
  private val name = "Haskell Stack REPL"

  override def createTemplateConfiguration(project: Project) = new HaskellConsoleConfiguration(name, project, this)

  override def getName: String = name

  override def getId: String = getName
}
