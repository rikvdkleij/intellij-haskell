package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._

class RemoveParensIntention extends PsiElementBaseIntentionAction {

  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    val selectionStartEnd = HaskellPsiUtil.getSelectionStartEnd(psiElement, editor)
    if (selectionStartEnd.isDefined) {
      for {
        (start, end) <- selectionStartEnd
      } yield {
        start.delete()
        end.delete()
      }
    } else {
      findFirstLastChildOfParent(psiElement) match {
        case Some((firstChild, lastChild)) =>
          if (firstChild.getNode.getElementType == HS_LEFT_PAREN && lastChild.getNode.getElementType == HS_RIGHT_PAREN) {
            firstChild.delete()
            lastChild.delete()
          }
        case None => ()
      }
    }

  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    HaskellPsiUtil.getSelectionStartEnd(psiElement, editor) match {
      case Some((start, end)) => psiElement.isWritable && start.getNode.getElementType == HS_LEFT_PAREN && end.getNode.getElementType == HS_RIGHT_PAREN
      case None =>
        findFirstLastChildOfParent(psiElement) match {
          case Some((firstChild, lastChild)) => firstChild.getNode.getElementType == HS_LEFT_PAREN && lastChild.getNode.getElementType == HS_RIGHT_PAREN
          case None => false
        }
    }
  }

  private def findFirstLastChildOfParent(psiElement: PsiElement) = {
    for {
      parent <- HaskellPsiUtil.findQualifiedNameParent(psiElement)
      firstChild <- Option(parent.getFirstChild)
      lastChild <- Option(parent.getLastChild)
    } yield {
      (firstChild, lastChild)
    }
  }

  override def getFamilyName: String = getText

  override def getText: String = "Remove parens around expression"
}
