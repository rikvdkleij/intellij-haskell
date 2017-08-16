package intellij.haskell.runconfig.console

import java.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations._
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.runconfig.HaskellStackConfigurationBase

import scala.collection.JavaConverters._

class HaskellConsoleConfiguration(name: String, project: Project, configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var stackTarget: String = ""
  val replCommand = "ghci"

  override def getConfigurationEditor = new HaskellConsoleConfigurationForm(getProject)

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellConsoleState(this, environment)

  def getStackTargetNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.getCabalStanzas.map(_.getTargetName)).asJava
  }

  def setStackTarget(target: String): Unit = {
    stackTarget = target
  }

  def getStackTarget: String = {
    stackTarget
  }
}
