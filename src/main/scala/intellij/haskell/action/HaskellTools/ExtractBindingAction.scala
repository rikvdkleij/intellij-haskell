package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.{ActionContext, ActionUtil}
import intellij.haskell.external.component.{HaskellToolsComponent, StackProjectManager}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil}

class ExtractBindingAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, StackProjectManager.isHaskellToolsAvailable)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach {
      case ActionContext(psiFile, _, project, selectionModel) =>
        HaskellFileUtil.saveFile(psiFile)

        for {
          moduleName <- HaskellPsiUtil.findModuleName(psiFile)
          selectionModel <- selectionModel
        } yield {
          val dialog = new RefactorDialog(project)

          if (!dialog.showAndGet()) return

          HaskellToolsComponent.extractBinding(project, psiFile, moduleName, selectionModel, dialog.getNewName)
        }
    }
  }
}
