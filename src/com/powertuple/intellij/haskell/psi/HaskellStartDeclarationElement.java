package com.powertuple.intellij.haskell.psi;

import com.intellij.psi.PsiElement;

public interface HaskellStartDeclarationElement extends HaskellCompositeElement{
    String getIdentifier();

    PsiElement getNameIdentifier();
}
