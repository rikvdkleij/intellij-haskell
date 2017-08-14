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

  private var testSuiteTargetName: String = ""
  private var testArguments: String = "--color"

  def getTestSuiteTargetNames: lang.Iterable[String] = {
    HaskellComponentsManager.findCabalInfos(project).flatMap(_.getTestSuites.map(_.getTargetName)).asJava
  }

  def setTestSuiteTargetName(targetName: String) {
    testSuiteTargetName = targetName
  }

  def getTestSuiteTargetName: String = {
    if (testSuiteTargetName.isEmpty) {
      getTestSuiteTargetNames.asScala.headOption.getOrElse("")
    } else {
      testSuiteTargetName
    }
  }

  def setTestArguments(testPattern: String) {
    this.testArguments = testPattern
  }

  def getTestArguments: String = {
    testArguments
  }

  override def getConfigurationEditor = new HaskellTestConfigurationForm(getProject)

  //https://github.com/commercialhaskell/stack/issues/731
  //https://github.com/commercialhaskell/stack/issues/2210
  override def getState(executor: Executor, environment: ExecutionEnvironment): HaskellStackStateBase = {
    val parameters = List("test", s"$testSuiteTargetName") ++ List("--test-arguments", getTestArguments)
    new HaskellStackStateBase(this, environment, parameters)
  }
}
