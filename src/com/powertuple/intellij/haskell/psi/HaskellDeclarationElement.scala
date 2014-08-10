package com.powertuple.intellij.haskell.psi

trait HaskellDeclarationElement extends HaskellCompositeElement {
  def getIdentifier: String

  def getIdentifierElement: HaskellNamedElement
}