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

public class HaskellGconSymImpl extends HaskellCompositeElementImpl implements HaskellGconSym {

  public HaskellGconSymImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitGconSym(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellConSym getConSym() {
    return findChildByClass(HaskellConSym.class);
  }

  @Override
  @Nullable
  public HaskellQconSym getQconSym() {
    return findChildByClass(HaskellQconSym.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public HaskellNamedElement getIdentifierElement() {
    return HaskellPsiImplUtil.getIdentifierElement(this);
  }

}
