// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellQqExpressionImpl extends HaskellCompositeElementImpl implements HaskellQqExpression {

  public HaskellQqExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQqExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellQName> getQNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQName.class);
  }

  @Override
  @NotNull
  public HaskellQuasiQuote getQuasiQuote() {
    return findNotNullChildByClass(HaskellQuasiQuote.class);
  }

  @Override
  @NotNull
  public List<HaskellSccPragma> getSccPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSccPragma.class);
  }

}
