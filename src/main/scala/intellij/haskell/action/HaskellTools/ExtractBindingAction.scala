package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.action.ActionUtil
import intellij.haskell.external.component.HaskellToolComponent
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

class ExtractBindingAction extends AnAction {
  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val file = actionContext.psiFile
      val vFile = HaskellFileUtil.findVirtualFile(file)
      val project = actionEvent.getProject

      HaskellFileUtil.saveFile(vFile)

      for {
        moduleName <- HaskellPsiUtil.findModuleName(file)
        selectionModel <- actionContext.selectionModel
      } yield {
        val dialog = new RefactDialog(project)

        if (!dialog.showAndGet()) return

        HaskellToolComponent.extractBinding(project, moduleName, selectionModel, dialog.getNewName)
        VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
      }
    })
  }
}
