package intellij.haskell.action.ghci

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.ActionUtil
import intellij.haskell.psi.{HaskellPsiUtil, HaskellTypes}
import intellij.haskell.runconfig.console.HaskellConsoleViewMap

class SendToConsoleAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    actionEvent.getPresentation.setEnabled(HaskellConsoleViewMap.getConsole(actionEvent.getProject).isDefined)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
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
