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

public class HaskellExpressionImpl extends HaskellCompositeElementImpl implements HaskellExpression {

  public HaskellExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellFirstLineExpression getFirstLineExpression() {
    return findChildByClass(HaskellFirstLineExpression.class);
  }

  @Override
  @NotNull
  public HaskellLastLineExpression getLastLineExpression() {
    return findNotNullChildByClass(HaskellLastLineExpression.class);
  }

  @Override
  @NotNull
  public List<HaskellLineExpression> getLineExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellLineExpression.class);
  }

}
