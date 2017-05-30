package intellij.haskell.notification

import com.intellij.ProjectTopics
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.{Project, ProjectBundle}
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.roots.{ModuleRootEvent, ModuleRootListener}
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

object HaskellSdkNotificationProvider {
  private val SdkNotificationPanelKey: Key[EditorNotificationPanel] = Key.create("Setup Haskell Stack SDK")
}

class HaskellSdkNotificationProvider(project: Project, notifications: EditorNotifications) extends EditorNotifications.Provider[EditorNotificationPanel] {
  private var previousSdkName: Option[String] = None

  project.getMessageBus.connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {

    override def beforeRootsChange(event: ModuleRootEvent): Unit = {
      previousSdkName = HaskellSdkType.getSdkName(project)
    }

    override def rootsChanged(event: ModuleRootEvent) {
      notifications.updateAllNotifications()
    }
  })

  override def getKey: Key[EditorNotificationPanel] = HaskellSdkNotificationProvider.SdkNotificationPanelKey

  override def createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      (previousSdkName, HaskellSdkType.getSdkName(project)) match {
        case (None, Some(_)) => createRestartPanel(project)
        case (Some(s1), Some(s2)) if s1 != s2 => createRestartPanel(project)
        case (_, None) => createSdkSetupPanel(project)
        case (_, _) => null
      }
    } else {
      null
    }
  }

  private def createSdkSetupPanel(project: Project) = {
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

  private def createRestartPanel(project: Project) = {
    createPanel(
      project,
      "Haskell Project SDK is changed",
      "Restart Haskell Stack REPLs",
      (project: Project) => () => {
        notifications.updateAllNotifications()
        StackProjectManager.restart(project)
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