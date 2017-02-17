package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import intellij.haskell.HaskellIcons

final class HaskellStackConfigurationType extends ConfigurationType {
  def getDisplayName = "Haskell Stack"

  def getConfigurationTypeDescription = "Haskell Stack configuration"

  def getIcon = HaskellIcons.HaskellSmallLogo

  def getId = "HaskellStackConfigurationType"

  def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](new HaskellConsoleConfigurationFactory(this))
}
