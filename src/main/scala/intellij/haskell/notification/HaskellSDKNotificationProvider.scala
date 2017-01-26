package intellij.haskell.notification

import com.intellij.ProjectTopics
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.{Project, ProjectBundle}
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.roots.{ModuleRootAdapter, ModuleRootEvent}
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

class HaskellSDKNotificationProvider(val myProject: Project, val notifications: EditorNotifications) extends EditorNotifications.Provider[EditorNotificationPanel] {
  private val KEY: Key[EditorNotificationPanel] = Key.create("Setup Haskell Stack SDK")
  private var currentHaskellSDKNameOption = HaskellSdkType.getCurrentHaskellSDKName(myProject)

  myProject.getMessageBus.connect(myProject).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
    override def rootsChanged(event: ModuleRootEvent) {
      notifications.updateAllNotifications()
    }
  })

  override def getKey: Key[EditorNotificationPanel] = KEY

  override def createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellStackProject(myProject, needShowBalloon = false)) {
      (for {
        sdkName <- HaskellSdkType.getCurrentHaskellSDKName(myProject)
        currentHaskellSDKName <- currentHaskellSDKNameOption
      } yield {
        if (sdkName != currentHaskellSDKName) {
          createPanel(
            myProject,
            "Haskell Project SDK is changed",
            "Restart Haskell Stack REPLs",
            (project: Project) => () => {
              StackProjectManager.restart(project, Option(ModuleUtilCore.findModuleForFile(file, project)))
              currentHaskellSDKNameOption = Some(sdkName)
              notifications.updateAllNotifications()
            }
          )
        } else {
          null
        }
      }).orNull
    } else {
      createPanel(
        myProject,
        ProjectBundle.message("project.sdk.not.defined"),
        ProjectBundle.message("project.sdk.setup"),
        (project: Project) => () => {
          Option(ProjectSettingsService.getInstance(project).chooseAndSetSdk()).foreach(sdk => {
            if (sdk.getSdkType == HaskellSdkType.getInstance)
              StackProjectManager.restart(project, Option(ModuleUtilCore.findModuleForFile(file, project)))
          })
        }
      )
    }
  }

  private def createPanel(project: Project, panelText: String, panelActionLabel: String, action: Project => Runnable): EditorNotificationPanel = {
    val panel: EditorNotificationPanel = new EditorNotificationPanel
    panel.setText(panelText)
    panel.createActionLabel(panelActionLabel, action(project))
    panel
  }
}