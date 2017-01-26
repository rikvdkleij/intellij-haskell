package intellij.haskell.notification

import java.util

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.{VirtualFile, VirtualFileManager}
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

private class ConfigFileWatcher(val notifications: EditorNotifications) extends BulkFileListener.Adapter {
  private val watchFiles = Seq("stack.yaml", "cabal.config", ".cabal")

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    import scala.collection.JavaConverters._
    if (events.asScala.exists(e => watchFiles.exists(e.getPath.endsWith(_) && !e.getPath.contains(HaskellModuleBuilder.LibName)))) {
      ConfigFileWatcherNotificationProvider.needShowPanel = true
      notifications.updateAllNotifications()
    }
  }
}

object ConfigFileWatcherNotificationProvider {
  private val KEY: Key[EditorNotificationPanel] = Key.create("Haskell config file watcher")
  var needShowPanel = false
}

class ConfigFileWatcherNotificationProvider(val myProject: Project, val notifications: EditorNotifications) extends EditorNotifications.Provider[EditorNotificationPanel] {
  myProject.getMessageBus.connect(myProject).subscribe(VirtualFileManager.VFS_CHANGES, new ConfigFileWatcher(notifications))

  override def getKey: Key[EditorNotificationPanel] = ConfigFileWatcherNotificationProvider.KEY

  override def createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellStackProject(myProject) && ConfigFileWatcherNotificationProvider.needShowPanel) {
      createPanel(myProject, file)
    } else {
      null
    }
  }

  private def createPanel(project: Project, file: VirtualFile): EditorNotificationPanel = {
    val panel: EditorNotificationPanel = new EditorNotificationPanel
    panel.setText("Config file is updated")
    panel.createActionLabel("Restart Haskell Stack REPLs", () => {
      ConfigFileWatcherNotificationProvider.needShowPanel = false
      notifications.updateAllNotifications()
      StackProjectManager.restart(project, Option(ModuleUtilCore.findModuleForFile(file, project)))
    })
    panel
  }
}
