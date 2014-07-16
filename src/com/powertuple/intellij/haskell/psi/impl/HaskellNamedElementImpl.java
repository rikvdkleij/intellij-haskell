package com.powertuple.intellij.haskell.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.powertuple.intellij.haskell.psi.HaskellNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class HaskellNamedElementImpl extends ASTWrapperPsiElement implements HaskellNamedElement {
    public HaskellNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
