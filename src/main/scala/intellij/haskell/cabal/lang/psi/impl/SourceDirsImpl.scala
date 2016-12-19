package intellij.haskell.cabal.lang.psi.impl

import com.intellij.psi.PsiElement
import intellij.haskell.cabal.lang.psi.CabalTypes
import intellij.haskell.psi.HaskellPsiUtil

trait SourceDirsImpl extends PsiElement {

  /** Retrieves the source dir paths as strings. */
  def getValue: Array[String] = {
    HaskellPsiUtil.getChildNodes(this, CabalTypes.SOURCE_DIR).map(_.getText)
  }
}
