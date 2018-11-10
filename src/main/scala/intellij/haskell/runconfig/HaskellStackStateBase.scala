package intellij.haskell.runconfig

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.{CommandLineState, GeneralCommandLine}
import com.intellij.execution.process.{KillableColoredProcessHandler, ProcessHandler, ProcessTerminatedListener}
import com.intellij.execution.runners.ExecutionEnvironment
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.JavaConverters._

class HaskellStackStateBase(val configuration: HaskellStackConfigurationBase, val environment: ExecutionEnvironment, val parameters: List[String]) extends CommandLineState(environment) {

  protected def startProcess: ProcessHandler = {
    val project = configuration.getProject

    HaskellSdkType.getStackBinaryPath(project) match {
      case Some(stackPath) =>
        val stackArgs = configuration.getStackArgs

        val commandLine = new GeneralCommandLine(stackPath)
          .withParameters(parameters.asJava)
          .withWorkDirectory(configuration.getWorkingDirPath)

        if (stackArgs.nonEmpty)
          commandLine.addParameters(stackArgs.split(" ").toList.asJava)

        val handler = new KillableColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        handler
      case None => throw new CantRunException("Invalid Haskell Stack SDK")
    }
  }
}
