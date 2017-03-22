package intellij.haskell.runconfig.console

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.{CommandLineState, GeneralCommandLine}
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.filters.TextConsoleBuilderImpl
import com.intellij.execution.process.{ProcessHandler, ProcessTerminatedListener}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.JavaConverters._

class HaskellConsoleState(val myConfig: HaskellConsoleConfiguration, val env: ExecutionEnvironment) extends CommandLineState(env) {
  val consoleBuilder = new TextConsoleBuilderImpl(myConfig.getProject) {
    override def getConsole: ConsoleView = {
      new HaskellConsoleView(myConfig.getProject)
    }
  }
  setConsoleBuilder(consoleBuilder)

  protected def startProcess: ProcessHandler = {
    val project = myConfig.getProject

    HaskellSdkType.getStackPath(project) match {
      case Some(stackPath) =>
        val consoleArgs = myConfig.getConsoleArgs
        val commandLine = new GeneralCommandLine(stackPath)
          .withParameters(myConfig.myCommand)
          .withWorkDirectory(myConfig.getWorkingDirPath)

        if (!consoleArgs.isEmpty)
          commandLine.addParameters(consoleArgs.split(" ").toList.asJava)

        val handler = new HaskellConsoleProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString(), consoleBuilder.getConsole.asInstanceOf[LanguageConsoleImpl])
        ProcessTerminatedListener.attach(handler)
        handler
      case None => throw new CantRunException("Invalid Haskell Stack SDK")
    }
  }
}
