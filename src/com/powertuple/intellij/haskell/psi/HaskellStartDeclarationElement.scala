package com.powertuple.intellij.haskell.psi

import com.intellij.psi.PsiElement

trait HaskellStartDeclarationElement extends HaskellCompositeElement {
  def getIdentifier: String

  def getNameIdentifier: PsiElement
}