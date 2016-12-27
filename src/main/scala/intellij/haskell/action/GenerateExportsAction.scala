package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.external.component.HaskellToolComponent
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

class GenerateExportsAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    ActionUtil.findActionContext(e).foreach(actionContext => {
      val file = actionContext.psiFile
      val vFile = HaskellFileUtil.findVirtualFile(file)

      HaskellFileUtil.saveFile(vFile)

      HaskellPsiUtil.findModuleName(file).foreach(moduleName => {
        HaskellToolComponent.generateExports(e.getProject, moduleName)
        VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
      })
    })
  }
}
