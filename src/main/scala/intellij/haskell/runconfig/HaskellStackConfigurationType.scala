package intellij.haskell.runconfig

import javax.swing.Icon

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import intellij.haskell.HaskellIcons
import intellij.haskell.runconfig.console.HaskellConsoleConfigurationFactory
import intellij.haskell.runconfig.run.HaskellRunConfigurationFactory
import intellij.haskell.runconfig.test.HaskellTestConfigurationFactory

class HaskellStackConfigurationType extends ConfigurationType {
  def getDisplayName: String = "Haskell Stack"

  def getConfigurationTypeDescription: String = "Haskell Stack configuration"

  def getIcon: Icon = HaskellIcons.HaskellLogo

  def getId = "HaskellStackConfigurationType"

  def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](
    new HaskellConsoleConfigurationFactory(this),
    new HaskellRunConfigurationFactory(this),
    new HaskellTestConfigurationFactory(this)
  )
}
