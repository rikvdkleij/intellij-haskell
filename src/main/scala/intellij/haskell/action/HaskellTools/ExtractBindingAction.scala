package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.action.{ActionContext, ActionUtil}
import intellij.haskell.external.component.HaskellToolComponent
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

class ExtractBindingAction extends AnAction {
  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    val project = actionEvent.getProject

    HaskellToolComponent.checkResolverForHaskellToolsAction(project, actionEvent, (actionEvent) => {
      ActionUtil.findActionContext(actionEvent).foreach {
        case ActionContext(file, _, _, selectionModel) =>
          val vFile = HaskellFileUtil.findVirtualFile(file)
          HaskellFileUtil.saveFile(vFile)

          for {
            moduleName <- HaskellPsiUtil.findModuleName(file)
            selectionModel <- selectionModel
          } yield {
            val dialog = new RefactDialog(project)

            if (!dialog.showAndGet()) return

            HaskellToolComponent.extractBinding(project, moduleName, selectionModel, dialog.getNewName)
            VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
          }
      }
    })
  }
}
