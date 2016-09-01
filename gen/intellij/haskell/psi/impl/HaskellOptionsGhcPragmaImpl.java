// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellOptionsGhcOption;
import intellij.haskell.psi.HaskellOptionsGhcPragma;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellOptionsGhcPragmaImpl extends HaskellCompositeElementImpl implements HaskellOptionsGhcPragma {

  public HaskellOptionsGhcPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitOptionsGhcPragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellOptionsGhcOption> getOptionsGhcOptionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellOptionsGhcOption.class);
  }

}
