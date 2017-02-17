package intellij.haskell.runconfig

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.{CommandLineState, GeneralCommandLine}
import com.intellij.execution.process.{KillableColoredProcessHandler, ProcessHandler, ProcessTerminatedListener}
import com.intellij.execution.runners.ExecutionEnvironment
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.JavaConverters._

class HaskellStackStateBase(val myConfig: HaskellStackConfigurationBase, val env: ExecutionEnvironment, val command: String) extends CommandLineState(env) {

  protected def startProcess: ProcessHandler = {
    val project = myConfig.getProject

    HaskellSdkType.getStackPath(project) match {
      case Some(stackPath) =>
        val consoleArgs = myConfig.getConsoleArgs
        val commandLine = new GeneralCommandLine(stackPath)
          .withParameters(command.split(" ").toList.asJava)
          .withWorkDirectory(myConfig.getWorkingDirPath)

        if (!consoleArgs.isEmpty)
          commandLine.addParameters(consoleArgs.split(" ").toList.asJava)

        val handler = new KillableColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        handler
      case None => throw new CantRunException("Invalid Haskell Stack SDK")
    }
  }
}
