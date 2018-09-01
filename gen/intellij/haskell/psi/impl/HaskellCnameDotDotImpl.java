// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellCname;
import intellij.haskell.psi.HaskellCnameDotDot;
import intellij.haskell.psi.HaskellDotDot;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellCnameDotDotImpl extends HaskellCompositeElementImpl implements HaskellCnameDotDot {

  public HaskellCnameDotDotImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitCnameDotDot(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellCname getCname() {
    return PsiTreeUtil.getChildOfType(this, HaskellCname.class);
  }

  @Override
  @Nullable
  public HaskellDotDot getDotDot() {
    return PsiTreeUtil.getChildOfType(this, HaskellDotDot.class);
  }

}
