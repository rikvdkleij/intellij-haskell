package intellij.haskell.runconfig.run

import java.util

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.cabal.query.CabalQuery
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

import scala.collection.JavaConverters._

class HaskellRunConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var myExecutable: String = ""

  def getExecutables: util.List[String] = {
    CabalQuery.getExecutableNames(project).getOrElse(List()).asJava
  }

  def setExecutable(consoleArgs: String) {
    myExecutable = consoleArgs
  }

  def getExecutable: String = {
    if (myExecutable.isEmpty) {
      CabalQuery.getExecutableNames(project).map(_.head).getOrElse(project.getName)
    } else {
      myExecutable
    }
  }

  override def getConfigurationEditor = new HaskellRunConfigurationForm(getProject)

  override def getState(executor: Executor, environment: ExecutionEnvironment) = new HaskellStackStateBase(this, environment, s"build --exec $myExecutable")
}
