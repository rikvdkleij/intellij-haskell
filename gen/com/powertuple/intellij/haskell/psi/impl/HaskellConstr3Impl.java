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

public class HaskellConstr3Impl extends HaskellCompositeElementImpl implements HaskellConstr3 {

  public HaskellConstr3Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitConstr3(this);
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
  public HaskellQcon getQcon() {
    return findNotNullChildByClass(HaskellQcon.class);
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

  @Override
  @NotNull
  public List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellUnpackNounpackPragma.class);
  }

}
