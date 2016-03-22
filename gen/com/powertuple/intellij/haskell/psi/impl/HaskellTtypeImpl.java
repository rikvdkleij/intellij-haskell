// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;
import com.powertuple.intellij.haskell.psi.*;

public class HaskellTtypeImpl extends HaskellCompositeElementImpl implements HaskellTtype {

  public HaskellTtypeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTtype(this);
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
  @Nullable
  public HaskellParallelArrayType getParallelArrayType() {
    return findChildByClass(HaskellParallelArrayType.class);
  }

  @Override
  @NotNull
  public List<HaskellQvar> getQvarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQvar.class);
  }

  @Override
  @NotNull
  public List<HaskellQvarOp> getQvarOpList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQvarOp.class);
  }

  @Override
  @NotNull
  public List<HaskellTtype> getTtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignatureDeclaration.class);
  }

}
