package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ModuleBasedConfiguration, RunConfigurationModule, RuntimeConfigurationException}
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.xmlb.XmlSerializer
import intellij.haskell.sdk.HaskellSdkType
import org.jdom.Element

import scala.collection.JavaConverters._

abstract class HaskellStackConfigurationBase(val name: String, val project: Project, val configurationFactory: ConfigurationFactory)
  extends ModuleBasedConfiguration[RunConfigurationModule](name, new RunConfigurationModule(project), configurationFactory) {
  private var myWorkingDirPath: String = ""
  private var myConsoleArgs: String = ""

  ModuleManager.getInstance(getProject).getModules.toList.headOption.foreach(m => setModule(m))

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
    if (selectedModule == null)
      throw new RuntimeConfigurationException("Haskell module is not selected")

    val projectSdk = ProjectRootManager.getInstance(getProject).getProjectSdk
    if (projectSdk == null || (projectSdk.getSdkType ne HaskellSdkType.getInstance))
      throw new RuntimeConfigurationException("Haskell Stack SDK is configured for the project")
  }

  def setWorkingDirPath(workingDirPath: String) {
    myWorkingDirPath = workingDirPath
  }

  def getWorkingDirPath: String = {
    if (myWorkingDirPath.isEmpty) {
      project.getBasePath
    } else {
      myWorkingDirPath
    }
  }

  def setConsoleArgs(consoleArgs: String) {
    myConsoleArgs = consoleArgs
  }

  def getConsoleArgs: String = myConsoleArgs
}
