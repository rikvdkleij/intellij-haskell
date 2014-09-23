package com.powertuple.intellij.haskell.psi

import com.intellij.navigation.NavigationItem

trait HaskellDeclarationElement extends HaskellCompositeElement with NavigationItem {
  def getIdentifierElements: Seq[HaskellNamedElement]
}