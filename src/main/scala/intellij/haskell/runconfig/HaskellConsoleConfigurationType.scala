package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import intellij.haskell.HaskellIcons

final class HaskellConsoleConfigurationType extends ConfigurationType {
  def getDisplayName = "Haskell Stack Shell"

  def getConfigurationTypeDescription = "Haskell Stack Shell configuration"

  def getIcon = HaskellIcons.HaskellSmallLogo

  def getId = "HaskellConsoleConfigurationType"

  def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](new HaskellConsoleConfigurationFactory(this))
}
