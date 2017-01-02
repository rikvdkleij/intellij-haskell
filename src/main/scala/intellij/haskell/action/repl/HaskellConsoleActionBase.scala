package intellij.haskell.action.repl

import com.intellij.execution.ExecutionHelper
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.NotNullFunction
import intellij.haskell.HaskellFile
import intellij.haskell.repl.{HaskellConsole, HaskellConsoleExecuteActionHandler, HaskellConsoleProcessHandler, HaskellConsoleRunner}
import java.lang.Boolean

object HaskellConsoleActionBase {

  final private class HaskellConsoleMatcher extends NotNullFunction[RunContentDescriptor, Boolean] {
    def fun(descriptor: RunContentDescriptor): Boolean = descriptor != null && descriptor.getExecutionConsole.isInstanceOf[HaskellConsole]
  }

  private def findRunningHaskellConsole(project: Project): HaskellConsoleProcessHandler = {
    val descriptors = ExecutionHelper.findRunningConsole(project, new HaskellConsoleMatcher).toArray()
    for (descriptor <- descriptors) {
      val handler = descriptor.asInstanceOf[RunContentDescriptor].getProcessHandler
      handler match {
        case h: HaskellConsoleProcessHandler => return h.asInstanceOf[HaskellConsoleProcessHandler]
        case _ =>
      }
    }
    null
  }

}

abstract class HaskellConsoleActionBase extends AnAction {
  protected def executeCommand(project: Project, command: String) {
    var processHandler = HaskellConsoleActionBase.findRunningHaskellConsole(project)
    // if a console isn't runnning, start one
    if (processHandler == null || processHandler.isProcessTerminated) {
      val module = RunHaskellConsoleAction.getModule(project)
      processHandler = HaskellConsoleRunner.run(module)
      if (processHandler == null) return
    }
    // implement a command
    val console = processHandler.getLanguageConsole
    console.setInputText(command)
    val editor = console.getCurrentEditor
    val caretModel = editor.getCaretModel
    caretModel.moveToOffset(command.length)
    new HaskellConsoleExecuteActionHandler(project, processHandler).runExecuteAction(console, executeImmediately = true)
  }

  override def update(e: AnActionEvent) {
    val presentation = e.getPresentation
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor == null) {
      presentation.setEnabled(false)
      return
    }
    val project = editor.getProject
    if (project == null) {
      presentation.setEnabled(false)
      return
    }
    val document = editor.getDocument
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
    if (psiFile == null || !psiFile.isInstanceOf[HaskellFile]) {
      presentation.setEnabled(false)
      return
    }
    val virtualFile = psiFile.getVirtualFile
    if (virtualFile == null || virtualFile.isInstanceOf[LightVirtualFile]) {
      presentation.setEnabled(false)
      return
    }
    val handler = HaskellConsoleActionBase.findRunningHaskellConsole(project)
    if (handler == null) {
      presentation.setEnabled(false)
      return
    }
    val console = handler.getLanguageConsole
    if (!console.isInstanceOf[HaskellConsole]) {
      presentation.setEnabled(false)
      return
    }
    presentation.setEnabled(true)
  }
}
