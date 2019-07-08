package intellij.haskell.runconfig.run

import java.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

import scala.jdk.CollectionConverters._

class HaskellRunConfiguration(name: String, project: Project, configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var executableName: String = ""
  private var programArgs: String = ""

  def getExecutableNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.executables.flatMap(_.name)).asJava
  }

  def setExecutable(executableName: String): Unit = {
    this.executableName = executableName
  }

  def getExecutable: String = {
    if (executableName.isEmpty) {
      getExecutableNames.asScala.headOption.getOrElse("")
    } else {
      executableName
    }
  }

  def setProgramArgs(programArgs: String): Unit = {
    this.programArgs = programArgs
  }

  def getProgramArgs: String = {
    this.programArgs
  }

  override def getConfigurationEditor = new HaskellRunConfigurationForm()

  override def getState(executor: Executor, environment: ExecutionEnvironment): HaskellStackStateBase = {
    val executableNameWithArgs = if (programArgs.isEmpty) {
      executableName
    } else {
      s"$executableName $programArgs"
    }
    new HaskellStackStateBase(this, environment, List("build", "--exec", executableNameWithArgs))
  }
}
