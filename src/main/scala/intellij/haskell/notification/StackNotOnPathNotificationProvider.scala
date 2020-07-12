package intellij.haskell.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.util.HaskellProjectUtil

object StackNotOnPathNotificationProvider {
  private val SdkNotificationPanelKey: Key[EditorNotificationPanel] = Key.create("Stack not on PATH warning")
}

class StackNotOnPathNotificationProvider extends EditorNotifications.Provider[EditorNotificationPanel] {

  override def getKey: Key[EditorNotificationPanel] = StackNotOnPathNotificationProvider.SdkNotificationPanelKey

  override def createNotificationPanel(virtualFile: VirtualFile, fileEditor: FileEditor, project: Project): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellProject(project) && HaskellProjectUtil.isSourceFile(project, virtualFile)) {
      if (StackCommandLine.isStackOnPath) {
        null
      } else {
        if (ProjectFileIndex.getInstance(project).isExcluded(virtualFile)) {
          null
        } else {
          createNotificationPanel(project)
        }
      }
    } else {
      null
    }
  }

  private def createNotificationPanel(project: Project) = {
    val notifications = EditorNotifications.getInstance(project)

    createPanel(
      project,
      (project: Project) => () => {
        StackProjectManager.restart(project)
        notifications.updateAllNotifications()
      }
    )
  }

  private def createPanel(project: Project, action: Project => Runnable): EditorNotificationPanel = {
    val panel = new EditorNotificationPanel
    panel.setText("Stack binary is not on the PATH")
    panel.createActionLabel("Restart", action(project))
    panel
  }
}