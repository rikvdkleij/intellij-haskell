package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._

class RemoveParensIntention extends PsiElementBaseIntentionAction {
  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    val (start, end) = HaskellPsiUtil.getSelectionStartEnd(psiElement, editor)
    start.delete()
    end.delete()
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    val (start, end) = HaskellPsiUtil.getSelectionStartEnd(psiElement, editor)
    psiElement.isWritable && start.getNode.getElementType == HS_LEFT_PAREN && end.getNode.getElementType == HS_RIGHT_PAREN
  }

  override def getFamilyName: String = getText

  override def getText: String = "Remove parens around expression"
}
