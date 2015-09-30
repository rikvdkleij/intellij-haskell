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

public class HaskellTypeFamilyTypeImpl extends HaskellCompositeElementImpl implements HaskellTypeFamilyType {

  public HaskellTypeFamilyTypeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitTypeFamilyType(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellContext> getContextList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellContext.class);
  }

  @Override
  @NotNull
  public List<HaskellTtype> getTtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeFamilyType1> getTypeFamilyType1List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeFamilyType1.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeFamilyType2> getTypeFamilyType2List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeFamilyType2.class);
  }

  @Override
  @NotNull
  public List<HaskellVars> getVarsList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellVars.class);
  }

}
