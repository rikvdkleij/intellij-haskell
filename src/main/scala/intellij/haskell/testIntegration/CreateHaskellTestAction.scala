package intellij.haskell.testIntegration

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.{CommandProcessor, WriteCommandAction}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.openapi.util.Computable
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiDirectory, PsiElement, PsiFileFactory, PsiManager}
import intellij.haskell.HaskellLanguage
import intellij.haskell.psi.HaskellClassDeclaration

/**
  * Orchestrate the test creation
  */
class CreateHaskellTestAction extends PsiElementBaseIntentionAction {
  override def invoke(project: Project, editor: Editor, element: PsiElement): Unit = {
    val testFileName = element.getContainingFile.getName.replace(".hs", "Spec.hs")
    val srcClass = PsiTreeUtil.getParentOfType(element, classOf[HaskellClassDeclaration])
    val srcModule = ModuleUtilCore.findModuleForPsiElement(element)
    val createTestDialog = new CreateHaskellTestDialog(project)

    // Prefill the Create Test dialog with decent default values
    createTestDialog.setFileName(testFileName)
    //TODO Build a decent default directory value (e.g. if the element is in ./src/A/B/C then a decent default test directory is ./test/A/B/C)
    createTestDialog.setTargetDir("")

    if (!createTestDialog.showAndGet) return

    CommandProcessor.getInstance().executeCommand(
      project,
      () => DumbService.getInstance(project).withAlternativeResolveEnabled(() => generateTest(project, createTestDialog)),
      CodeInsightBundle.message("intention.create.test"),
      this
    )
  }

  override def isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean = true

  override def getFamilyName: String = CodeInsightBundle.message("intention.create.test")

  def generateTest(project: Project, createHaskellTestDialog: CreateHaskellTestDialog): PsiElement = {
    val baseDirectory: PsiDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir)
    val file = PsiFileFactory.getInstance(project).createFileFromText(HaskellLanguage.Instance, "abcd")
    val createFile: Computable[PsiElement] = () => baseDirectory.findSubdirectory("test").add(file)
    val createdFile = WriteCommandAction.runWriteCommandAction(project, createFile)
    createdFile
  }
}
