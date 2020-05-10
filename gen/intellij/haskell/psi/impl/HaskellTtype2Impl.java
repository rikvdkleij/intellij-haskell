// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellPragma;
import intellij.haskell.psi.HaskellTtype;
import intellij.haskell.psi.HaskellTtype2;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellTtype2Impl extends HaskellCompositeElementImpl implements HaskellTtype2 {

  public HaskellTtype2Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTtype2(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellPragma> getPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
  }

  @Override
  @NotNull
  public List<HaskellTtype> getTtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
  }

}
