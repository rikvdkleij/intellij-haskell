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

public class HaskellOpImpl extends HaskellCompositeElementImpl implements HaskellOp {

  public HaskellOpImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitOp(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellQconOp getQconOp() {
    return findChildByClass(HaskellQconOp.class);
  }

  @Override
  @Nullable
  public HaskellQvarOp getQvarOp() {
    return findChildByClass(HaskellQvarOp.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public HaskellNamedElement getIdentifierElement() {
    return HaskellPsiImplUtil.getIdentifierElement(this);
  }

}
