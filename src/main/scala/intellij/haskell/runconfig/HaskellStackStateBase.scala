package intellij.haskell.runconfig

import com.intellij.execution.configurations.{CommandLineState, GeneralCommandLine}
import com.intellij.execution.process.{KillableColoredProcessHandler, ProcessHandler, ProcessTerminatedListener}
import com.intellij.execution.runners.ExecutionEnvironment
import intellij.haskell.GlobalInfo
import intellij.haskell.external.execution.StackCommandLine.StackPath

import scala.jdk.CollectionConverters._

class HaskellStackStateBase(val configuration: HaskellStackConfigurationBase, val environment: ExecutionEnvironment, val parameters: List[String]) extends CommandLineState(environment) {

  protected def startProcess: ProcessHandler = {
    val stackArgs = configuration.getStackArgs
    val commandLine = new GeneralCommandLine(StackPath)
      .withParameters(parameters.asJava)
      .withWorkDirectory(configuration.getWorkingDirPath)
      .withEnvironment(GlobalInfo.pathVariables)

    if (stackArgs.nonEmpty)
      commandLine.addParameters(stackArgs.split(" ").toList.asJava)

    val handler = new KillableColoredProcessHandler(commandLine)
    ProcessTerminatedListener.attach(handler)
    handler
  }
}
