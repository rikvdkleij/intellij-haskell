package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi.{HaskellElementFactory, HaskellPsiUtil}
import intellij.haskell.util.HaskellProjectUtil

class AddParensIntention extends PsiElementBaseIntentionAction {

  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    val selectionStartEnd = HaskellPsiUtil.getSelectionStartEnd(psiElement, editor)
    if (selectionStartEnd.isDefined) {
      for {
        (start, end) <- selectionStartEnd
      } yield {
        if (start.getNode.getElementType != HS_NEWLINE) {
          val left = HaskellElementFactory.getLeftParenElement(project)
          val right = HaskellElementFactory.getRightParenElement(project)
          start.getParent.addBefore(left, start)
          end.getParent.addAfter(right, start)
        }
      }
    } else {
      AddParensIntention.addParens(project, psiElement)
    }
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    HaskellProjectUtil.isHaskellProject(project) && (HaskellPsiUtil.getSelectionStartEnd(psiElement, editor) match {
      case Some((start, _)) if start.getNode.getElementType != HS_NEWLINE => psiElement.isWritable
      case _ => false
    })
  }

  override def getFamilyName: String = getText

  override def getText: String = "Add parens around expression"
}

object AddParensIntention {

  def addParens(project: Project, psiElement: PsiElement): PsiElement = {
    val left = HaskellElementFactory.getLeftParenElement(project)
    val right = HaskellElementFactory.getRightParenElement(project)
    psiElement.getParent.addBefore(left, psiElement)
    psiElement.getParent.addAfter(right, psiElement)
  }
}