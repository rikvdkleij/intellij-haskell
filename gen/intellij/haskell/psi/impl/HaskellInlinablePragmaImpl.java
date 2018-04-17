// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellGeneralPragmaContent;
import intellij.haskell.psi.HaskellInlinablePragma;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellInlinablePragmaImpl extends HaskellCompositeElementImpl implements HaskellInlinablePragma {

  public HaskellInlinablePragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitInlinablePragma(this);
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
