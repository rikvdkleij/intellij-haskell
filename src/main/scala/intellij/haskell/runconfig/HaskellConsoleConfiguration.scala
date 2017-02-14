package intellij.haskell.runconfig

import com.intellij.execution.Executor
import com.intellij.execution.configurations._
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModuleRootManager, ProjectRootManager}
import com.intellij.util.xmlb.XmlSerializer
import intellij.haskell.sdk.HaskellSdkType
import org.jdom.Element

import scala.collection.JavaConverters._

final class HaskellConsoleConfiguration(val name: String, val project: Project, val configurationFactory: ConfigurationFactory)
  extends ModuleBasedConfiguration[RunConfigurationModule](name, new RunConfigurationModule(project), configurationFactory) {
  private var myWorkingDirPath: String = _
  private var myConsoleArgs: String = _

  def getConfigurationEditor = new HaskellConsoleConfigurationForm(getProject, getConfigurationModule.getModule)

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellConsoleCommandLineState(this, environment)

  override def getValidModules: java.util.Collection[Module] = ModuleManager.getInstance(getProject).getModules.toList.asJava

  override def writeExternal(element: Element) {
    super.writeExternal(element)
    XmlSerializer.serializeInto(this, element)
  }

  override def readExternal(element: Element) {
    super.readExternal(element)
    XmlSerializer.deserializeInto(this, element)
  }

  override def checkConfiguration() {
    val selectedModule = getConfigurationModule.getModule
    if (selectedModule == null) {
      val projectSdk = ProjectRootManager.getInstance(getProject).getProjectSdk
      if (projectSdk == null || (projectSdk.getSdkType ne HaskellSdkType.getInstance)) throw new RuntimeConfigurationException("Neither Haskell module selected nor Haskell Stack SDK is configured for the project")
    }
    else {
      val moduleSdk = ModuleRootManager.getInstance(selectedModule).getSdk
      if (moduleSdk == null || (moduleSdk.getSdkType ne HaskellSdkType.getInstance)) throw new RuntimeConfigurationException("Haskell Stack SDK is not configured for the selected module")
    }
  }

  def setWorkingDirPath(workingDirPath: String) {
    myWorkingDirPath = workingDirPath
  }

  def getWorkingDirPath: String = myWorkingDirPath

  def setConsoleArgs(consoleArgs: String) {
    myConsoleArgs = consoleArgs
  }

  def getConsoleArgs: String = myConsoleArgs
}
