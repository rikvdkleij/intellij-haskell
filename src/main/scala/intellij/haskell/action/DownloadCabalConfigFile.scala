package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.external.component.HaskellComponentsManager.refreshGlobalProjectInfo
import intellij.haskell.external.component.PathComponent

class DownloadCabalConfigFile extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    PathComponent.removeCabalConfig(project)
    PathComponent.downloadCabalConfig(project).foreach(b => {
      if (b) {
        refreshGlobalProjectInfo(project)
      }
    })
  }
}
