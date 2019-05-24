// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellExpression;
import intellij.haskell.psi.HaskellLetAbstraction;
import intellij.haskell.psi.HaskellLetLayout;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellLetAbstractionImpl extends HaskellCompositeElementImpl implements HaskellLetAbstraction {

  public HaskellLetAbstractionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitLetAbstraction(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, HaskellExpression.class);
  }

  @Override
  @NotNull
  public HaskellLetLayout getLetLayout() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellLetLayout.class));
  }

}
