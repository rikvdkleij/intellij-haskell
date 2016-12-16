// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellExportImpl extends HaskellCompositeElementImpl implements HaskellExport {

  public HaskellExportImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitExport(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellCname> getCnameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCname.class);
  }

  @Override
  @Nullable
  public HaskellDotDotParens getDotDotParens() {
    return PsiTreeUtil.getChildOfType(this, HaskellDotDotParens.class);
  }

  @Override
  @Nullable
  public HaskellModid getModid() {
    return PsiTreeUtil.getChildOfType(this, HaskellModid.class);
  }

  @Override
  @Nullable
  public HaskellQCon getQCon() {
    return PsiTreeUtil.getChildOfType(this, HaskellQCon.class);
  }

}
