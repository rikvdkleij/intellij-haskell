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

public class HaskellWhereClauseImpl extends HaskellCompositeElementImpl implements HaskellWhereClause {

  public HaskellWhereClauseImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitWhereClause(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellTopDeclaration getTopDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellTopDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellTopDeclarationLine> getTopDeclarationLineList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTopDeclarationLine.class);
  }

}
