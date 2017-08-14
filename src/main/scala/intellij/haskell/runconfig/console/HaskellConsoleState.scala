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

class HaskellConsoleState(val configuration: HaskellConsoleConfiguration, val env: ExecutionEnvironment) extends CommandLineState(env) {

  val consoleBuilder = new TextConsoleBuilderImpl(configuration.getProject) {
    override def getConsole: ConsoleView = {
      new HaskellConsoleView(configuration.getProject, configuration.getStackTarget)
    }
  }
  setConsoleBuilder(consoleBuilder)

  protected def startProcess: ProcessHandler = {
    val project = configuration.getProject

    HaskellSdkType.getStackPath(project) match {
      case Some(stackPath) =>
        val consoleArgs = configuration.getStackArgs
        val stackTargetName = configuration.getStackTarget
        val commandLine = new GeneralCommandLine(stackPath)
          .withParameters(configuration.replCommand)
          .withWorkDirectory(configuration.getWorkingDirPath)

        if (consoleArgs.nonEmpty)
          commandLine.addParameters(consoleArgs.split(" ").toList.asJava)

        if (stackTargetName.nonEmpty) {
          commandLine.addParameter(stackTargetName)
        }

        val handler = new HaskellConsoleProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString(), consoleBuilder.getConsole.asInstanceOf[LanguageConsoleImpl])
        ProcessTerminatedListener.attach(handler)
        handler
      case None => throw new CantRunException("Invalid Haskell Stack SDK")
    }
  }
}
