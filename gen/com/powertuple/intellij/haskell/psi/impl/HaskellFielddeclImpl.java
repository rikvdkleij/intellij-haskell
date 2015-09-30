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

public class HaskellFielddeclImpl extends HaskellCompositeElementImpl implements HaskellFielddecl {

  public HaskellFielddeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitFielddecl(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGtycon getGtycon() {
    return findChildByClass(HaskellGtycon.class);
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
  @Nullable
  public HaskellUnpackNounpackPragma getUnpackNounpackPragma() {
    return findChildByClass(HaskellUnpackNounpackPragma.class);
  }

  @Override
  @NotNull
  public HaskellVars getVars() {
    return findNotNullChildByClass(HaskellVars.class);
  }

}
