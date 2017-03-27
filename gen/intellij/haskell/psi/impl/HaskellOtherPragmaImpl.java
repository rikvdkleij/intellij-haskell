// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    return PsiTreeUtil.getChildOfType(this, HaskellAnnPragma.class);
  }

  @Override
  @Nullable
  public HaskellCfilesPragma getCfilesPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellCfilesPragma.class);
  }

  @Override
  @Nullable
  public HaskellConstantFoldedPragma getConstantFoldedPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellConstantFoldedPragma.class);
  }

  @Override
  @Nullable
  public HaskellDeprecatedWarnPragma getDeprecatedWarnPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellDeprecatedWarnPragma.class);
  }

  @Override
  @Nullable
  public HaskellDummyPragma getDummyPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellDummyPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlinablePragma getInlinablePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlinablePragma.class);
  }

  @Override
  @Nullable
  public HaskellInlineFusedPragma getInlineFusedPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlineFusedPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlineInnerPragma getInlineInnerPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlineInnerPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlinePragma getInlinePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlinePragma.class);
  }

  @Override
  @Nullable
  public HaskellLinePragma getLinePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellLinePragma.class);
  }

  @Override
  @Nullable
  public HaskellMinimalPragma getMinimalPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellMinimalPragma.class);
  }

  @Override
  @Nullable
  public HaskellNoinlinePragma getNoinlinePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellNoinlinePragma.class);
  }

  @Override
  @Nullable
  public HaskellOverlapPragma getOverlapPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellOverlapPragma.class);
  }

  @Override
  @Nullable
  public HaskellRulesPragma getRulesPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellRulesPragma.class);
  }

  @Override
  @Nullable
  public HaskellSpecializePragma getSpecializePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellSpecializePragma.class);
  }

}
