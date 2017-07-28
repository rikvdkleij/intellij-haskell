// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellConstr3;
import intellij.haskell.psi.HaskellQName;
import intellij.haskell.psi.HaskellSubConstr2;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellConstr3Impl extends HaskellCompositeElementImpl implements HaskellConstr3 {

  public HaskellConstr3Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitConstr3(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellQName getQName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellQName.class));
  }

  @Override
  @NotNull
  public List<HaskellSubConstr2> getSubConstr2List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSubConstr2.class);
  }

}
