package intellij.haskell.action.repl

import java.lang.Boolean

import com.intellij.execution.ExecutionHelper
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.util.NotNullFunction
import intellij.haskell.repl.{HaskellConsole, HaskellConsoleExecuteActionHandler, HaskellConsoleProcessHandler, HaskellConsoleRunner}

object HaskellREPLActionBase {

  final private class HaskellConsoleMatcher extends NotNullFunction[RunContentDescriptor, Boolean] {
    def fun(descriptor: RunContentDescriptor): Boolean = descriptor != null && descriptor.getExecutionConsole.isInstanceOf[HaskellConsole]
  }

  private def findRunningHaskellConsole(project: Project): Option[HaskellConsoleProcessHandler] = {
    val descriptors = ExecutionHelper.findRunningConsole(project, new HaskellConsoleMatcher).toArray()
    descriptors.find(descriptor => {
      val handler = descriptor.asInstanceOf[RunContentDescriptor].getProcessHandler
      handler.isInstanceOf[HaskellConsoleProcessHandler]
    }).map(_.asInstanceOf[RunContentDescriptor].getProcessHandler.asInstanceOf[HaskellConsoleProcessHandler])
  }
}

abstract class HaskellREPLActionBase extends AnAction {
  private def doExecuteCommand(project: Project, processHandler: HaskellConsoleProcessHandler, command: String): Unit = {
    val console = processHandler.getLanguageConsole
    console.setInputText(command)
    val editor = console.getCurrentEditor
    val caretModel = editor.getCaretModel
    caretModel.moveToOffset(command.length)
    new HaskellConsoleExecuteActionHandler(project, processHandler).runExecuteAction(console, executeImmediately = true)
  }

  protected def executeCommand(project: Project, command: String) {
    HaskellREPLActionBase.findRunningHaskellConsole(project) match {
      case Some(processHandler) if !processHandler.isProcessTerminated => doExecuteCommand(project, processHandler, command)
      case _ =>
        for {
          module <- RunHaskellREPLAction.getModule(project)
          processHandler <- HaskellConsoleRunner.run(module)
        } yield doExecuteCommand(project, processHandler, command)
    }
  }
}
