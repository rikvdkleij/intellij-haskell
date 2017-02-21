package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import intellij.haskell.HaskellIcons
import intellij.haskell.runconfig.action.HaskellStackCommandConfigurationFactory
import intellij.haskell.runconfig.console.HaskellConsoleConfigurationFactory
import intellij.haskell.runconfig.run.HaskellRunConfigurationFactory
import intellij.haskell.runconfig.test.HaskellTestConfigurationFactory

class HaskellStackConfigurationType extends ConfigurationType {
  def getDisplayName = "Haskell Stack"

  def getConfigurationTypeDescription = "Haskell Stack configuration"

  def getIcon = HaskellIcons.HaskellSmallLogo

  def getId = "HaskellStackConfigurationType"

  def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](
    new HaskellConsoleConfigurationFactory(this),
    new HaskellRunConfigurationFactory(this),
    new HaskellTestConfigurationFactory(this),
    new HaskellStackCommandConfigurationFactory(this)
  )
}
