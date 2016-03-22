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
import scala.Option;

public class HaskellQvarImpl extends HaskellCompositeElementImpl implements HaskellQvar {

  public HaskellQvarImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQvar(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellQvarDotSym getQvarDotSym() {
    return findChildByClass(HaskellQvarDotSym.class);
  }

  @Override
  @Nullable
  public HaskellQvarId getQvarId() {
    return findChildByClass(HaskellQvarId.class);
  }

  @Override
  @Nullable
  public HaskellQvarSym getQvarSym() {
    return findChildByClass(HaskellQvarSym.class);
  }

  @Override
  @Nullable
  public HaskellVarDotSym getVarDotSym() {
    return findChildByClass(HaskellVarDotSym.class);
  }

  @Override
  @Nullable
  public HaskellVarId getVarId() {
    return findChildByClass(HaskellVarId.class);
  }

  @Override
  @Nullable
  public HaskellVarSym getVarSym() {
    return findChildByClass(HaskellVarSym.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public HaskellNamedElement getIdentifierElement() {
    return HaskellPsiImplUtil.getIdentifierElement(this);
  }

  public Option<String> getQualifier() {
    return HaskellPsiImplUtil.getQualifier(this);
  }

}
