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

public class HaskellInstImpl extends HaskellCompositeElementImpl implements HaskellInst {

  public HaskellInstImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitInst(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGtycon getGtycon() {
    return findChildByClass(HaskellGtycon.class);
  }

  @Override
  @NotNull
  public List<HaskellInstvar> getInstvarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellInstvar.class);
  }

  @Override
  @Nullable
  public HaskellQvar getQvar() {
    return findChildByClass(HaskellQvar.class);
  }

}
