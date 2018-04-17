// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellGeneralPragmaContent;
import intellij.haskell.psi.HaskellMinimalPragma;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellMinimalPragmaImpl extends HaskellCompositeElementImpl implements HaskellMinimalPragma {

  public HaskellMinimalPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitMinimalPragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGeneralPragmaContent getGeneralPragmaContent() {
    return PsiTreeUtil.getChildOfType(this, HaskellGeneralPragmaContent.class);
  }

}
