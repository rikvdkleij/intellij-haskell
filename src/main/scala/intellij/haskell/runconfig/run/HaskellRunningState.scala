package intellij.haskell.runconfig.run

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.{CommandLineState, GeneralCommandLine}
import com.intellij.execution.process.{KillableColoredProcessHandler, ProcessHandler, ProcessTerminatedListener}
import com.intellij.execution.runners.ExecutionEnvironment
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.JavaConverters._

class HaskellRunningState(val myConfig: HaskellRunConfiguration, val env: ExecutionEnvironment) extends CommandLineState(env) {

  protected def startProcess: ProcessHandler = {
    val project = myConfig.getProject

    HaskellSdkType.getStackPath(project) match {
      case Some(stackPath) =>
        val consoleArgs = myConfig.getConsoleArgs
        val commandLine = new GeneralCommandLine(stackPath)
          .withParameters(myConfig.myCommand.split(" ").toList.asJava)
          .withParameters(consoleArgs.split(" ").toList.asJava)
          .withWorkDirectory(myConfig.getWorkingDirPath)
        val handler = new KillableColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        handler
      case None => throw new CantRunException("Invalid Haskell Stack SDK")
    }
  }
}
