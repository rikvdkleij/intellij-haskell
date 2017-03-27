// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellInlinelikePragmaImpl extends HaskellCompositeElementImpl implements HaskellInlinelikePragma {

  public HaskellInlinelikePragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitInlinelikePragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellInlineFusedPragma getInlineFusedPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlineFusedPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlineInnerPragma getInlineInnerPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlineInnerPragma.class);
  }

  @Override
  @Nullable
  public HaskellInlinePragma getInlinePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellInlinePragma.class);
  }

  @Override
  @Nullable
  public HaskellNoinlinePragma getNoinlinePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellNoinlinePragma.class);
  }

}
