// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static intellij.haskell.psi.HaskellTypes.*;
import intellij.haskell.psi.*;

public class HaskellLetAbstractionImpl extends HaskellExpressionImpl implements HaskellLetAbstraction {

  public HaskellLetAbstractionImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitLetAbstraction(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellCdecl> getCdeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCdecl.class);
  }

  @Override
  @Nullable
  public HaskellExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, HaskellExpression.class);
  }

}
