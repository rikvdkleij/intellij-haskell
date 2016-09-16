// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellQuasiQuoteImpl extends HaskellCompositeElementImpl implements HaskellQuasiQuote {

  public HaskellQuasiQuoteImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQuasiQuote(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellInsideQqExpression getInsideQqExpression() {
    return findChildByClass(HaskellInsideQqExpression.class);
  }

  @Override
  @Nullable
  public HaskellSimpletype getSimpletype() {
    return findChildByClass(HaskellSimpletype.class);
  }

  @Override
  @NotNull
  public List<HaskellTopDeclaration> getTopDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTopDeclaration.class);
  }

}
