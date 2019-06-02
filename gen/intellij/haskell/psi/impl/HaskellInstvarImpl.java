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

public class HaskellInstvarImpl extends HaskellCompositeElementImpl implements HaskellInstvar {

  public HaskellInstvarImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitInstvar(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellQName getQName() {
    return PsiTreeUtil.getChildOfType(this, HaskellQName.class);
  }

  @Override
  @Nullable
  public HaskellTtype getTtype() {
    return PsiTreeUtil.getChildOfType(this, HaskellTtype.class);
  }

}
