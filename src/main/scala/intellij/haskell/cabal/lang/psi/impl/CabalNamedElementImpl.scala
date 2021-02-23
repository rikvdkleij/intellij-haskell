package intellij.haskell.cabal.lang.psi.impl

import com.intellij.psi.{PsiElement, PsiReference}
import intellij.haskell.cabal.lang.psi.{CabalNamedElement, CabalReference}

trait CabalNamedElementImpl extends CabalNamedElement {

  def getVariants: Array[AnyRef]

  def resolve(): Option[PsiElement]

  override def getReference: PsiReference = {
    new CabalReference(this, getTextRange)
  }
}
