package intellij.haskell.util

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton

object HaskellUIUtil {
  def installWorkingDirectoryChooser(on: TextFieldWithBrowseButton, project: Project) {
    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor
    on.addBrowseFolderListener("Choose Working Directory", null, project, descriptor)
  }
}
