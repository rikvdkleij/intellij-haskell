// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellImportId;
import intellij.haskell.psi.HaskellImportIdsSpec;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellImportIdsSpecImpl extends HaskellCompositeElementImpl implements HaskellImportIdsSpec {

  public HaskellImportIdsSpecImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitImportIdsSpec(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellImportId> getImportIdList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellImportId.class);
  }

}
