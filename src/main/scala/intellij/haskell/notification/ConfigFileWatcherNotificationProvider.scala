package intellij.haskell.notification

import java.util

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.{VirtualFile, VirtualFileManager}
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.util.HaskellProjectUtil

object ConfigFileWatcherNotificationProvider {
  private val ConfigFileWatcherKey: Key[EditorNotificationPanel] = Key.create("Haskell config file watcher")
  var showNotification = false
}

class ConfigFileWatcherNotificationProvider(project: Project, notifications: EditorNotifications) extends EditorNotifications.Provider[EditorNotificationPanel] {
  project.getMessageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, new ConfigFileWatcher(project, notifications))

  override def getKey: Key[EditorNotificationPanel] = ConfigFileWatcherNotificationProvider.ConfigFileWatcherKey

  override def createNotificationPanel(virtualFile: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (HaskellProjectUtil.isHaskellProject(project) && ConfigFileWatcherNotificationProvider.showNotification) {
      ConfigFileWatcherNotificationProvider.showNotification = false
      createPanel(project, virtualFile)
    } else {
      null
    }
  }

  private def createPanel(project: Project, file: VirtualFile): EditorNotificationPanel = {
    val panel = new EditorNotificationPanel
    panel.setText("Haskell project configuration file is updated")
    panel.createActionLabel("Restart Haskell Stack REPLs", () => {
      notifications.updateAllNotifications()
      StackProjectManager.restart(project)
    })
    panel.createActionLabel("Ignore", () => {
      notifications.updateAllNotifications()
    })
    panel
  }
}

private class ConfigFileWatcher(project: Project, notifications: EditorNotifications) extends BulkFileListener {
  private val watchFiles = HaskellProjectUtil.findStackFile(project).toIterable ++ HaskellProjectUtil.findCabalFiles(project)

  override def before(events: util.List[_ <: VFileEvent]): Unit = {}

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    import scala.collection.JavaConverters._
    if (events.asScala.exists(e => watchFiles.exists(_.getAbsolutePath == e.getPath))) {
      ConfigFileWatcherNotificationProvider.showNotification = true
      notifications.updateAllNotifications()
    }
  }
}
