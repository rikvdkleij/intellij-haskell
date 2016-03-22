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

public class HaskellOtherPragmaImpl extends HaskellCompositeElementImpl implements HaskellOtherPragma {

  public HaskellOtherPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitOtherPragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellAnnPragma getAnnPragma() {
    return findChildByClass(HaskellAnnPragma.class);
  }

  @Override
  @Nullable
  public HaskellDeprecatedWarnPragma getDeprecatedWarnPragma() {
    return findChildByClass(HaskellDeprecatedWarnPragma.class);
  }

  @Override
  @Nullable
  public HaskellDummyPragma getDummyPragma() {
    return findChildByClass(HaskellDummyPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlinablePragma getInlinablePragma() {
    return findChildByClass(HaskellInlinablePragma.class);
  }

  @Override
  @Nullable
  public HaskellInlinePragma getInlinePragma() {
    return findChildByClass(HaskellInlinePragma.class);
  }

  @Override
  @Nullable
  public HaskellLinePragma getLinePragma() {
    return findChildByClass(HaskellLinePragma.class);
  }

  @Override
  @Nullable
  public HaskellMinimalPragma getMinimalPragma() {
    return findChildByClass(HaskellMinimalPragma.class);
  }

  @Override
  @Nullable
  public HaskellNoinlinePragma getNoinlinePragma() {
    return findChildByClass(HaskellNoinlinePragma.class);
  }

  @Override
  @Nullable
  public HaskellRulesPragma getRulesPragma() {
    return findChildByClass(HaskellRulesPragma.class);
  }

  @Override
  @Nullable
  public HaskellSpecializePragma getSpecializePragma() {
    return findChildByClass(HaskellSpecializePragma.class);
  }

}
