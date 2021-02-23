package intellij.haskell.module

import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import com.intellij.util.ObjectUtils

import scala.jdk.CollectionConverters._

class StackProjectOpenProcessor extends ProjectOpenProcessor {

  override def canOpenProject(file: VirtualFile): Boolean = {
    if (file.isDirectory) {
      val files = getFileChildren(file).toSeq
      files.exists(c => c.getName == "stack.yaml")
    } else {
      false
    }
  }

  private def getFileChildren(file: VirtualFile): Array[VirtualFile] = ObjectUtils.chooseNotNull(file.getChildren, VirtualFile.EMPTY_ARRAY)

  override def getName: String = "Haskell Stack Project Processor"

  override def doOpenProject(virtualFile: VirtualFile, projectToClose: Project, forceOpenInNewFrame: Boolean): Project = {
    val providers = ImportModuleAction.getProviders(null)
    val wizard = ImportModuleAction.createImportWizard(null, null, virtualFile, providers.asScala.toSeq: _*)
    if (wizard == null || wizard.getStepCount > 0 && !wizard.showAndGet) return null

    val project = wizard.getWizardContext.getProject
    ImportModuleAction.createFromWizard(project, wizard)
    project
  }
}
