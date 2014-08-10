package com.powertuple.intellij.haskell.psi

trait HaskellStartDeclarationElement extends HaskellCompositeElement {
  def getIdentifier: String

  def getIdentifierElement: HaskellNamedElement
}