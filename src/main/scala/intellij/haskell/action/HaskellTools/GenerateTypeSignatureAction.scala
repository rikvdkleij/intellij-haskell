package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.{ActionContext, ActionUtil}
import intellij.haskell.external.component.{HaskellToolsComponent, StackProjectManager}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil}

class GenerateTypeSignatureAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, StackProjectManager.isHaskellToolsAvailable)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach {
      case ActionContext(psiFile, editor, project, selectionModel) =>
        HaskellFileUtil.saveFile(psiFile)

        HaskellPsiUtil.findModuleName(psiFile).foreach(moduleName => {
          HaskellToolsComponent.generateTypeSignature(project, psiFile, moduleName, editor, selectionModel)
        })
    }
  }
}
