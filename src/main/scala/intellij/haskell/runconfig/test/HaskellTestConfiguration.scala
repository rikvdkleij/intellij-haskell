package intellij.haskell.runconfig.test

import java.util

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.cabal.query.CabalQuery
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

import scala.collection.JavaConverters._

class HaskellTestConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var myTestsuite: String = ""

  def getTestsuites: util.List[String] = CabalQuery.getTestsuiteNames(project).getOrElse(List()).asJava

  def setTestsuite(consoleArgs: String) {
    myTestsuite = consoleArgs
  }

  def getTestsuite: String = {
    if (myTestsuite.isEmpty) {
      CabalQuery.getTestsuiteNames(project).map(_.head).getOrElse(s"${project.getName}-test")
    } else {
      myTestsuite
    }
  }

  override def getConfigurationEditor = new HaskellTestConfigurationForm(getProject)

  //https://github.com/commercialhaskell/stack/issues/731
  override def getState(executor: Executor, environment: ExecutionEnvironment): HaskellStackStateBase = {
    val packageName: String = CabalQuery.getPackageName(project).getOrElse(project.getName)
    new HaskellStackStateBase(this, environment, s"test $packageName:$myTestsuite")
  }
}
