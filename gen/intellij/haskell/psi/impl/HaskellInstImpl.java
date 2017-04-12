// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellGtycon;
import intellij.haskell.psi.HaskellInst;
import intellij.haskell.psi.HaskellInstvar;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellInstImpl extends HaskellCompositeElementImpl implements HaskellInst {

  public HaskellInstImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitInst(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellGtycon> getGtyconList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellGtycon.class);
  }

  @Override
  @NotNull
  public List<HaskellInstvar> getInstvarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellInstvar.class);
  }

}
