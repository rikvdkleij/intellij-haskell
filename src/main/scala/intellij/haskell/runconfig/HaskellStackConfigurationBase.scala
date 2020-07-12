package intellij.haskell.runconfig

import com.intellij.execution.configurations.{ConfigurationFactory, ModuleBasedConfiguration, RunConfigurationModule, RuntimeConfigurationException}
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element

import scala.jdk.CollectionConverters._

abstract class HaskellStackConfigurationBase(name: String, project: Project, configurationFactory: ConfigurationFactory)
  extends ModuleBasedConfiguration[RunConfigurationModule, HaskellStackConfigurationBase](name, new RunConfigurationModule(project), configurationFactory) {

  private var workingDirPath: String = ""
  private var stackArgs: String = ""

  ModuleManager.getInstance(getProject).getModules.toList.headOption.foreach(m => setModule(m))

  override def getValidModules: java.util.Collection[Module] = ModuleManager.getInstance(getProject).getModules.toList.asJava

  override def writeExternal(element: Element): Unit = {
    super.writeExternal(element)
    XmlSerializer.serializeInto(this, element)
  }

  override def readExternal(element: Element): Unit = {
    super.readExternal(element)
    XmlSerializer.deserializeInto(this, element)
  }

  override def checkConfiguration(): Unit = {
    val selectedModule = getConfigurationModule.getModule
    if (selectedModule == null)
      throw new RuntimeConfigurationException("Haskell module is not selected")
  }

  def setWorkingDirPath(workingDirPath: String): Unit = {
    this.workingDirPath = workingDirPath
  }

  def getWorkingDirPath: String = {
    if (workingDirPath.isEmpty) {
      project.getBasePath
    } else {
      workingDirPath
    }
  }

  def setStackArgs(stackArgs: String): Unit = {
    this.stackArgs = stackArgs
  }

  def getStackArgs: String = stackArgs
}
