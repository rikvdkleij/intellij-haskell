package intellij.haskell.cabal.lang.psi.impl

import com.intellij.psi.PsiElement
import intellij.haskell.cabal.lang.psi.{CabalTypes, IdentList}
import intellij.haskell.psi.HaskellPsiUtil

trait IdentListFieldImpl extends PsiElement {

  /** Retrieves the extension names as strings. */
  def getValue: Array[String] = HaskellPsiUtil.getChildOfType(this, classOf[IdentList]) match {
    case None => Array.empty
    case Some(el) => HaskellPsiUtil.getChildNodes(el, CabalTypes.IDENT).map(_.getText)
  }
}

