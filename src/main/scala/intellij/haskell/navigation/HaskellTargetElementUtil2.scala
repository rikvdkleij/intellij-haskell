package intellij.haskell.navigation

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.PsiElement

class HaskellTargetElementUtil2 extends TargetElementEvaluatorEx2 {

  override def isAcceptableNamedParent(parent: PsiElement): Boolean = {
    false
  }
}