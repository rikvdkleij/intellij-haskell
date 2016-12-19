package intellij.haskell.cabal.lang.psi

import com.intellij.psi.PsiElement
import intellij.haskell.cabal.lang.psi
import intellij.haskell.psi.HaskellPsiUtil

/**
 * Utilities for traversing the Cabal Psi tree.
 */
object CabalPsiUtil {

  def getFieldContext(el: PsiElement): Option[psi.CabalFieldElement] = {
    HaskellPsiUtil.collectFirstParent(el) { case el: psi.CabalFieldElement => el }
  }
}
