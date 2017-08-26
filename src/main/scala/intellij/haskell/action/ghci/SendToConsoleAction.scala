package intellij.haskell.action.ghci

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.ActionUtil
import intellij.haskell.psi.{HaskellPsiUtil, HaskellTypes}
import intellij.haskell.runconfig.console.HaskellConsoleViewMap
import intellij.haskell.util.HaskellEditorUtil

class SendToConsoleAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile

      val lookupText = Option(editor.getSelectionModel.getSelectedText).orElse {
        HaskellPsiUtil.untilNonWhitespaceBackwards(Option(psiFile.findElementAt(editor.getCaretModel.getOffset))).map {
          case e if e.getNode.getElementType == HaskellTypes.HS_COMMENT =>
            e.getText.stripPrefix("--").trim()

          case e if e.getNode.getElementType == HaskellTypes.HS_NCOMMENT =>
            e.getText.stripPrefix("{-").stripSuffix("-}").trim()

          case e =>
            e.getText
        }
      }

      for {
        text <- lookupText
        console <- HaskellConsoleViewMap.getConsole(actionContext.project)
      } yield {
        console.executeCommand(text)
      }
    })
  }

}
