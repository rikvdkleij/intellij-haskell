package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.util.HaskellEditorUtil

class UpdateHaskellToolsAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => !StackProjectManager.isInstallingHaskellTools(project) && !StackProjectManager.isInitializing(project) && !StackProjectManager.isPreloadingAllLibraryIdentifiers(project))
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    Option(actionEvent.getProject).foreach(project => {
      HaskellNotificationGroup.logInfoEvent(project, "Updating Haskell Tools")
      StackProjectManager.installHaskellTools(project, update = true)
    })
  }
}
