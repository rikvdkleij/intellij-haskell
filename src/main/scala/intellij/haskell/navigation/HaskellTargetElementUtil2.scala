package intellij.haskell.navigation

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.PsiElement
import intellij.haskell.psi.HaskellNamedElement

class HaskellTargetElementUtil2 extends TargetElementEvaluatorEx2 {

  override def getNamedElement(element: PsiElement): PsiElement = {
    if (element.isInstanceOf[HaskellNamedElement]) {
      element
    } else {
      null
    }
  }

  override def isAcceptableNamedParent(parent: PsiElement): Boolean = {
    false
  }
}