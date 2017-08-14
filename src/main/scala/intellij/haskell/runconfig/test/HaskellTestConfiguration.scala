package intellij.haskell.runconfig.test

import java.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.runconfig.{HaskellStackConfigurationBase, HaskellStackStateBase}

import scala.collection.JavaConverters._

class HaskellTestConfiguration(override val name: String, override val project: Project, override val configurationFactory: ConfigurationFactory)
  extends HaskellStackConfigurationBase(name, project, configurationFactory) {

  private var testsuiteTargetName: String = ""
  private var testFilter: String = ""

  def getTestsuiteTargetNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.getTestSuites.map(_.getTargetName)).asJava
  }

  def setTestsuiteTargetName(targetName: String) {
    testsuiteTargetName = targetName
  }

  def getTestsuiteName: String = {
    if (testsuiteTargetName.isEmpty) {
      getTestsuiteTargetNames.asScala.headOption.getOrElse("")
    } else {
      testsuiteTargetName
    }
  }

  def setTestFilter(testFilter: String) {
    this.testFilter = testFilter
  }

  def getTestFilter: String = {
    testFilter
  }

  override def getConfigurationEditor = new HaskellTestConfigurationForm(getProject)

  //https://github.com/commercialhaskell/stack/issues/731
  //https://github.com/commercialhaskell/stack/issues/2210
  override def getState(executor: Executor, environment: ExecutionEnvironment): HaskellStackStateBase = {
    val parameters = List("test", s"$testsuiteTargetName") ++
      (if (getTestFilter.isEmpty) List() else List("--test-arguments", "-m \"" + getTestFilter + "\""))
    new HaskellStackStateBase(this, environment, parameters)
  }
}
