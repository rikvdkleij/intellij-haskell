package intellij.haskell.notification

import com.intellij.ProjectTopics
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.{Project, ProjectBundle}
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.roots.{ModuleRootAdapter, ModuleRootEvent}
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiFile, PsiManager}
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.action.RestartStackReplsAction
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.{HaskellFileType, HaskellLanguage}

class HaskellSDKNotificationProvider(val myProject: Project, val notifications: EditorNotifications) extends EditorNotifications.Provider[EditorNotificationPanel] {
  private val KEY: Key[EditorNotificationPanel] = Key.create("Setup Erlang SDK")
  private var currentHaskellSDKName = HaskellSdkType.getCurrentHaskellSDKName(myProject)

  myProject.getMessageBus.connect(myProject).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
    override def rootsChanged(event: ModuleRootEvent) {
      notifications.updateAllNotifications()
    }
  })

  override def getKey: Key[EditorNotificationPanel] = KEY

  override def createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (!file.getFileType.isInstanceOf[HaskellFileType]) return null
    val psiFile: PsiFile = PsiManager.getInstance(myProject).findFile(file)
    if (psiFile == null || (psiFile.getLanguage != HaskellLanguage.Instance)) return null
    if (HaskellSdkType.isHaskellSDK(myProject)) {
      val sdkName = HaskellSdkType.getCurrentHaskellSDKName(myProject)
      if (sdkName != currentHaskellSDKName) {
        currentHaskellSDKName = sdkName
        createPanel(
          myProject,
          "Haskell Project SDK is changed",
          "Restart Haskell Stack REPLs",
          (project: Project) => () => {
            RestartStackReplsAction.restart(project)
            notifications.updateAllNotifications()
          }
        )
      } else {
        null
      }
    } else {
      createPanel(
        myProject,
        ProjectBundle.message("project.sdk.not.defined"),
        ProjectBundle.message("project.sdk.setup"),
        (project: Project) => () => {
          Option(ProjectSettingsService.getInstance(project).chooseAndSetSdk()).foreach(_ => {
            RestartStackReplsAction.restart(project)
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