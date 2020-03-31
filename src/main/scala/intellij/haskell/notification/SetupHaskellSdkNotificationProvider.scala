package intellij.haskell.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.{Project, ProjectBundle}
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

object SetupHaskellSdkNotificationProvider {
  private val SdkNotificationPanelKey: Key[EditorNotificationPanel] = Key.create("Setup Haskell Stack SDK")
}

class SetupHaskellSdkNotificationProvider extends EditorNotifications.Provider[EditorNotificationPanel] {

  override def getKey: Key[EditorNotificationPanel] = SetupHaskellSdkNotificationProvider.SdkNotificationPanelKey

  override def createNotificationPanel(virtualFile: VirtualFile, fileEditor: FileEditor, project: Project): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellProject(project) && HaskellProjectUtil.isSourceFile(project, virtualFile)) {
      HaskellProjectUtil.findModuleForVirtualFile(project, virtualFile).flatMap(m => HaskellSdkType.getSdkName(project, m)) match {
        case None =>
          if (ProjectFileIndex.getInstance(project).isExcluded(virtualFile)) {
            null
          } else {
            createSdkSetupPanel(project)
          }
        case _ => null
      }
    } else {
      null
    }
  }

  private def createSdkSetupPanel(project: Project) = {
    val notifications = EditorNotifications.getInstance(project)

    createPanel(
      project,
      ProjectBundle.message("project.sdk.not.defined"),
      ProjectBundle.message("project.sdk.setup"),
      (project: Project) => () => {
        Option(ProjectSettingsService.getInstance(project).chooseAndSetSdk()).foreach(sdk => {
          if (sdk.getSdkType == HaskellSdkType.getInstance) {
            StackProjectManager.restart(project)
          }
          notifications.updateAllNotifications()
        })
      }
    )
  }

  private def createPanel(project: Project, panelText: String, panelActionLabel: String, action: Project => Runnable): EditorNotificationPanel = {
    val panel = new EditorNotificationPanel
    panel.setText(panelText)
    panel.createActionLabel(panelActionLabel, action(project))
    panel
  }
}