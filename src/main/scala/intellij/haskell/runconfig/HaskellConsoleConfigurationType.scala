package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import intellij.haskell.HaskellIcons

final class HaskellConsoleConfigurationType extends ConfigurationType {
  def getDisplayName = "Haskell Stack Command"

  def getConfigurationTypeDescription = "Haskell Stack command run configuration"

  def getIcon = HaskellIcons.HaskellSmallLogo

  def getId = "HaskellStackRunConfigurationType"

  def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](new HaskellConsoleConfigurationFactory(this))
}
