package intellij.haskell.cabal.lang.psi.impl

import com.intellij.navigation.NavigationItem
import com.intellij.psi.{PsiElement, PsiNameIdentifierOwner, PsiReference}

import intellij.haskell.cabal.lang.psi.CabalReference

trait CabalNamedElementImpl extends PsiNameIdentifierOwner with NavigationItem {

  def getVariants: Array[AnyRef]

  def resolve(): PsiElement

  override def getReference: PsiReference = {
    new CabalReference(this, getTextRange)
  }
}
