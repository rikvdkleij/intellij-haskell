package intellij.haskell.codeinsight

import com.intellij.codeInsight.hint.ImplementationTextSelectioner
import com.intellij.psi.PsiElement
import intellij.haskell.psi.{HaskellPsiUtil, HaskellTopDeclarationLine}

class HaskellImplementationTextSelectioner extends ImplementationTextSelectioner {

  override def getTextStartOffset(element: PsiElement): Int = {
    getTextRange(element)._1
  }

  override def getTextEndOffset(element: PsiElement): Int = {
    getTextRange(element)._2
  }

  private def getTextRange(element: PsiElement) = {
    HaskellPsiUtil.findTopDeclarationLineParent(element) match {
      case Some(dp) => Option(dp.getNextSibling) match {
        case Some(e: HaskellTopDeclarationLine) => (dp.getTextRange.getStartOffset, e.getTextRange.getEndOffset)
        case _ => Option(dp.getPrevSibling) match {
          case Some(e: HaskellTopDeclarationLine) => (e.getTextRange.getStartOffset, dp.getTextRange.getEndOffset)
          case _ => (dp.getTextRange.getStartOffset, dp.getTextRange.getEndOffset)
        }
      }
      case None => (element.getTextRange.getStartOffset, element.getTextRange.getEndOffset)
    }
  }

}
