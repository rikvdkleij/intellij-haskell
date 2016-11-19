package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.psi.HaskellTypes._

class RemoveParensIntention extends PsiElementBaseIntentionAction {
  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    val psiFile = psiElement.getContainingFile
    val start = psiFile.findElementAt(editor.getSelectionModel.getSelectionStart)
    val end = psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1)
    HaskellNotificationGroup.logWarning(s"============ start element text ${psiElement.getText}")
    HaskellNotificationGroup.logWarning(s"============ end element text ${psiElement.getText}")
    start.delete()
    end.delete()
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    val psiFile = psiElement.getContainingFile
    val start = psiFile.findElementAt(editor.getSelectionModel.getSelectionStart)
    val end = psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1)
    HaskellNotificationGroup.logWarning(s"=============== ${start.getNode.getElementType == HS_LEFT_PAREN} ${psiElement.isWritable} ${end.getNode.getElementType == HS_RIGHT_PAREN}")
    psiElement.isWritable && start.getNode.getElementType == HS_LEFT_PAREN && end.getNode.getElementType == HS_RIGHT_PAREN
  }

  override def getFamilyName: String = getText

  override def getText: String = "Remove parens around expression"
}
