package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.action.ActionUtil
import intellij.haskell.external.component.HaskellToolComponent
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

class InlineBindingAction extends AnAction {
  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    val project = actionEvent.getProject

    HaskellToolComponent.checkResolverForHaskellToolsAction(project, actionEvent, (actionEvent) => {
      ActionUtil.findActionContext(actionEvent).foreach(actionContext => {

        val file = actionContext.psiFile
        val vFile = HaskellFileUtil.findVirtualFile(file)
        val project = actionContext.project

        HaskellFileUtil.saveFile(vFile)

        for {
          moduleName <- HaskellPsiUtil.findModuleName(file)
          selectionModel <- actionContext.selectionModel
        } yield {
          HaskellToolComponent.inlineBinding(project, moduleName, selectionModel)
          VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
        }
      })
    })
  }
}
