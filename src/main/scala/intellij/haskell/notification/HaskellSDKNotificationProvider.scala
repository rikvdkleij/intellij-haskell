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
    if (HaskellSdkType.isHaskellSDK(myProject)) return null
    createPanel(myProject, psiFile)
  }

  private def createPanel(project: Project, file: PsiFile): EditorNotificationPanel = {
    val panel: EditorNotificationPanel = new EditorNotificationPanel
    panel.setText(ProjectBundle.message("project.sdk.not.defined"))
    panel.createActionLabel(ProjectBundle.message("project.sdk.setup"), () => {
      Option(ProjectSettingsService.getInstance(project).chooseAndSetSdk()).foreach(_ => {
        RestartStackReplsAction.restart(myProject)
      })
    })
    panel
  }
}