package intellij.haskell.runconfig.console

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx

class HaskellConsoleExecuteAction extends AnAction {
  override def update(actionEvent: AnActionEvent): Unit = {
    val presentation = actionEvent.getPresentation
    val editor: Editor = actionEvent.getData(CommonDataKeys.EDITOR)
    if (!editor.isInstanceOf[EditorEx] || editor.asInstanceOf[EditorEx].isRendererMode) {
      presentation.setEnabled(false)
    } else {
      HaskellConsoleViewDict.getInstance.getConsole(editor) match {
        case Some(consoleView) => presentation.setEnabledAndVisible(consoleView.isRunning)
        case None => presentation.setEnabled(false)
      }
    }
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    for {
      editor <- Option(actionEvent.getData(CommonDataKeys.EDITOR))
      consoleView <- HaskellConsoleViewDict.getInstance.getConsole(editor)
    } yield consoleView.execute()
  }
}
