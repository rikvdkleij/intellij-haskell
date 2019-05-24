// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellConid;
import intellij.haskell.psi.HaskellConop;
import intellij.haskell.psi.HaskellConsym;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellConopImpl extends HaskellCNameElementImpl implements HaskellConop {

  public HaskellConopImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitConop(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellConid getConid() {
    return PsiTreeUtil.getChildOfType(this, HaskellConid.class);
  }

  @Override
  @Nullable
  public HaskellConsym getConsym() {
    return PsiTreeUtil.getChildOfType(this, HaskellConsym.class);
  }

  @Override
  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

}
