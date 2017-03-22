package intellij.haskell.action.HaskellTools

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.action.{ActionContext, ActionUtil}
import intellij.haskell.external.component.HaskellToolComponent
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

class GenerateExportsAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject

    HaskellToolComponent.checkResolverForHaskellToolsAction(project, e, (actionEvent) => {
      ActionUtil.findActionContext(actionEvent).foreach {
        case ActionContext(file, _, _, _) =>
          val vFile = HaskellFileUtil.findVirtualFile(file)
          HaskellFileUtil.saveFile(vFile)

          HaskellPsiUtil.findModuleName(file).foreach(moduleName => {
            HaskellToolComponent.generateExports(project, moduleName)
            VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
          })
      }
    })
  }
}
