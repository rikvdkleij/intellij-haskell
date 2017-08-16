package intellij.haskell.runconfig.run

import java.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

import scala.collection.JavaConverters._

class HaskellRunConfiguration(name: String, project: Project, configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var executableName: String = ""

  def getExecutableNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.getExecutables.flatMap(_.getName)).asJava
  }

  def setExecutable(executableName: String) {
    this.executableName = executableName
  }

  def getExecutable: String = {
    if (executableName.isEmpty) {
      getExecutableNames.asScala.headOption.getOrElse("")
    } else {
      executableName
    }
  }

  override def getConfigurationEditor = new HaskellRunConfigurationForm()

  override def getState(executor: Executor, environment: ExecutionEnvironment) =
    new HaskellStackStateBase(this, environment, List("build", "--exec", executableName))
}
