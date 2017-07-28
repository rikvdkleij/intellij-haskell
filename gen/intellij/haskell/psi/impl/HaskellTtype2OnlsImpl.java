// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellTtype2;
import intellij.haskell.psi.HaskellTtype2Onls;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellTtype2OnlsImpl extends HaskellCompositeElementImpl implements HaskellTtype2Onls {

  public HaskellTtype2OnlsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTtype2Onls(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellTtype2 getTtype2() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellTtype2.class));
  }

}
