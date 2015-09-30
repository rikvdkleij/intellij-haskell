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

public class HaskellConstr2Impl extends HaskellCompositeElementImpl implements HaskellConstr2 {

  public HaskellConstr2Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitConstr2(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellQconOp getQconOp() {
    return findNotNullChildByClass(HaskellQconOp.class);
  }

  @Override
  @NotNull
  public List<HaskellSubConstr2> getSubConstr2List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSubConstr2.class);
  }

  @Override
  @NotNull
  public List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellUnpackNounpackPragma.class);
  }

}
