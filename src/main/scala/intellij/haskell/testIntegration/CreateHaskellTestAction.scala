package intellij.haskell.testIntegration

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.{Project, ProjectUtil}
import com.intellij.openapi.ui.Messages
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.action.CreateHaskellFileAction
import intellij.haskell.psi.HaskellModuleDeclaration

/**
  * Orchestrate the test module creation
  */
class CreateHaskellTestAction extends PsiElementBaseIntentionAction {

  /**
    * Build a decent default directory value (e.g. if source module is ./src/A/Bc/Defg.hs then a decent default test directory is ./test/A/Bc/DefgSpec.hs)
    */
  override def invoke(project: Project, editor: Editor, element: PsiElement): Unit = {
    PsiTreeUtil.findChildOfType(element.getContainingFile, classOf[HaskellModuleDeclaration])
      .getModuleName
      .foreach(moduleName => {
        val testModuleName = moduleName + "Spec"
        // Note: We can't directly reuse CreateFileFromTemplateDialog because it doesn't allow to inject a default value
        val createTestDialog = new CreateHaskellTestDialog(project)
        // Prefill the Create Test dialog with decent default values
        createTestDialog.setModuleName(testModuleName)

        val testRootDirectory = PsiManager.getInstance(project).findDirectory(ProjectUtil.guessProjectDir(project).findChild("test"))

        val testTemplate = FileTemplateManager.getInstance(project).getInternalTemplate("Haskell Test Module")

        val createTestFileAction = new CreateHaskellFileAction()

        // If the user left the dialog by any other mean than "OK" then abort everything
        if (!createTestDialog.showAndGet) return
        val testFileCreation: Runnable = () => createTestFileAction.createFileFromTemplate(createTestDialog.getModuleName, testTemplate, testRootDirectory)
        try {
          WriteCommandAction.runWriteCommandAction(project, testFileCreation)
        } catch {
          case e: Throwable => Messages.showErrorDialog(project, s"Error while creating test module: ${e.getMessage}", "Failed to create test module");
        }
      })
  }

  override def isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean = true

  override def getFamilyName: String = CodeInsightBundle.message("intention.create.test")
}
