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

  private var executableName: Option[String] = None
  private var programArgs: String = ""

  def getExecutableNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.executables.flatMap(_.name)).asJava
  }

  def setExecutableName(executableName: String): Unit = {
    this.executableName = Option(executableName).orElse(getExecutableNames.asScala.headOption)
  }

  def getExecutableName: String = {
    executableName.orNull
  }

  def setProgramArgs(programArgs: String): Unit = {
    this.programArgs = programArgs
  }

  def getProgramArgs: String = {
    this.programArgs
  }

  override def getConfigurationEditor = new HaskellRunConfigurationForm()

  override def getState(executor: Executor, environment: ExecutionEnvironment): HaskellStackStateBase = {
    executableName match {
      case Some(name) =>
        val executableNameWithArgs = if (programArgs.isEmpty) {
          name
        } else {
          s"$name $programArgs"
        }
        new HaskellStackStateBase(this, environment, List("build", "--exec", executableNameWithArgs))
      case None => null
    }
  }
}
