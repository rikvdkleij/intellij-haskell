package intellij.haskell.notification

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ui.configuration.{SdkPopupBuilder, SdkPopupFactory}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import intellij.haskell.HaskellLanguage
import intellij.haskell.sdk.HaskellSdkType

object HaskellProjectSdkSetupValidator {
  final val Instance = new HaskellProjectSdkSetupValidator

  private def preparePopup(project: Project): SdkPopupBuilder = {
    SdkPopupFactory.newBuilder.withProject(project).withSdkTypeFilter((sdkTypeId: SdkTypeId) => sdkTypeId.isInstanceOf[HaskellSdkType]).updateProjectSdkFromSelection()
  }
}

class HaskellProjectSdkSetupValidator extends ProjectSdkSetupValidator {
  private final val Message = "Haskell SDK is not defined. Please setup Haskell SDK and then reopen project."

  override def isApplicableFor(project: Project, file: VirtualFile): Boolean = {
    Option(PsiManager.getInstance(project).findFile(file)) match {
      case Some(psiFile) => psiFile.getLanguage.isKindOf(HaskellLanguage.Instance)
      case _ => false
    }
  }

  override def getErrorMessage(project: Project, file: VirtualFile): String = {
    Option(ModuleUtilCore.findModuleForFile(file, project)) match {
      case Some(module) =>
        Option(ModuleRootManager.getInstance(module).getSdk) match {
          case None => Message
          case Some(sdk) if sdk.getSdkType != HaskellSdkType.getInstance => Message
          case _ => null
        }
      case None => null
    }
  }

  override def getFixHandler(project: Project, file: VirtualFile): EditorNotificationPanel.ActionHandler = {
    HaskellProjectSdkSetupValidator.preparePopup(project).buildEditorNotificationPanelHandler
  }
}