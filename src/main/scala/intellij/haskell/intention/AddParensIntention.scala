package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiWhiteSpace}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi.{HaskellElementFactory, HaskellPsiUtil}

class AddParensIntention extends PsiElementBaseIntentionAction {
  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    for {
      left <- HaskellElementFactory.getLeftParenElement(project)
      right <- HaskellElementFactory.getRightParenElement(project)
      (start, end) <- HaskellPsiUtil.getSelectionStartEnd(psiElement, editor)
    } yield {
      val addParens = (start: PsiElement, end: PsiElement) => {
        start.getParent.addBefore(left, start)
        end.getParent.addAfter(right, end)
      }

      addParens(start, end)
    }
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    HaskellPsiUtil.getSelectionStartEnd(psiElement, editor) match {
      case None => false
      case Some((start, end)) =>
        psiElement.isWritable && (start.getNode.getElementType != HS_LEFT_PAREN || end.getNode.getElementType != HS_RIGHT_PAREN)
    }
  }

  override def getFamilyName: String = getText

  override def getText: String = "Add parens around expression"
}
