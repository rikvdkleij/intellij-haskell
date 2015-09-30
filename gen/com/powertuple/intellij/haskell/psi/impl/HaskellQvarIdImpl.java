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

public class HaskellQvarIdImpl extends HaskellCompositeElementImpl implements HaskellQvarId {

  public HaskellQvarIdImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitQvarId(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellQualifier getQualifier() {
    return findNotNullChildByClass(HaskellQualifier.class);
  }

  @Override
  @NotNull
  public HaskellVarId getVarId() {
    return findNotNullChildByClass(HaskellVarId.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public HaskellNamedElement getIdentifierElement() {
    return HaskellPsiImplUtil.getIdentifierElement(this);
  }

}
