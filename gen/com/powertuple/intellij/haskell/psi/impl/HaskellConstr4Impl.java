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

public class HaskellConstr4Impl extends HaskellCompositeElementImpl implements HaskellConstr4 {

  public HaskellConstr4Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitConstr4(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellGconSym getGconSym() {
    return findNotNullChildByClass(HaskellGconSym.class);
  }

  @Override
  @NotNull
  public HaskellQcon getQcon() {
    return findNotNullChildByClass(HaskellQcon.class);
  }

  @Override
  @NotNull
  public HaskellQvar getQvar() {
    return findNotNullChildByClass(HaskellQvar.class);
  }

  @Override
  @NotNull
  public List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellUnpackNounpackPragma.class);
  }

}
