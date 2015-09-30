// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;
import com.powertuple.intellij.haskell.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;

public class HaskellModIdImpl extends HaskellNamedElementImpl implements HaskellModId {

  public HaskellModIdImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitModId(this);
    else super.accept(visitor);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return HaskellPsiImplUtil.setName(this, newName);
  }

  public HaskellNamedElement getNameIdentifier() {
    return HaskellPsiImplUtil.getNameIdentifier(this);
  }

  public PsiReference getReference() {
    return HaskellPsiImplUtil.getReference(this);
  }

  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  public SearchScope getUseScope() {
    return HaskellPsiImplUtil.getUseScope(this);
  }

}
