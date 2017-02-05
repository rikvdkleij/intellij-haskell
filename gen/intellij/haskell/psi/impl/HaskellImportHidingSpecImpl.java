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

public class HaskellImportHidingSpecImpl extends HaskellCompositeElementImpl implements HaskellImportHidingSpec {

  public HaskellImportHidingSpecImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitImportHidingSpec(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellImportHiding getImportHiding() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellImportHiding.class));
  }

  @Override
  @NotNull
  public List<HaskellImportId> getImportIdList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellImportId.class);
  }

}
