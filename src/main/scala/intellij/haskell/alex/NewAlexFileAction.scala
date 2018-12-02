package intellij.haskell.alex

import com.intellij.ide.actions.{CreateFileFromTemplateAction, CreateFileFromTemplateDialog}
import com.intellij.ide.fileTemplates.actions.AttributesDefaults
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog
import com.intellij.ide.fileTemplates.{FileTemplate, FileTemplateManager}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.{PsiDirectory, PsiFile}
import icons.HaskellIcons

/**
  * @author ice1000
  */
object NewAlexFileAction {
  final val NAME = "Alex Source File"
}

/**
  * @author ice1000
  */
class NewAlexFileAction extends CreateFileFromTemplateAction(NewAlexFileAction.NAME, "Create Alex source file", HaskellIcons.AlexLogo) {
  override def buildDialog(project: Project, dir: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder): Unit = {
    builder
      .setTitle("Create " + NewAlexFileAction.NAME)
      .addKind("Alex file", HaskellIcons.AlexLogo, "Alex Source")
  }

  override def getActionName(psiDirectory: PsiDirectory, s: String, s1: String): String = {
    NewAlexFileAction.NAME
  }

  override def createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory): PsiFile = try {
    val className = FileUtilRt.getNameWithoutExtension(name)
    val project = dir.getProject
    val properties = FileTemplateManager.getInstance(project).getDefaultProperties
    properties.put("NAME", className)
    new CreateFromTemplateDialog(project, dir, template, new AttributesDefaults(className).withFixedName(true), properties)
      .create()
      .getContainingFile
  } catch {
    case _: Throwable => null
  }

}
