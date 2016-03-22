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
import scala.collection.Seq;

public class HaskellSimpletypeImpl extends HaskellCompositeElementImpl implements HaskellSimpletype {

  public HaskellSimpletypeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitSimpletype(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGconSym getGconSym() {
    return findChildByClass(HaskellGconSym.class);
  }

  @Override
  @Nullable
  public HaskellParallelArrayType getParallelArrayType() {
    return findChildByClass(HaskellParallelArrayType.class);
  }

  @Override
  @Nullable
  public HaskellQcon getQcon() {
    return findChildByClass(HaskellQcon.class);
  }

  @Override
  @Nullable
  public HaskellQvar getQvar() {
    return findChildByClass(HaskellQvar.class);
  }

  @Override
  @Nullable
  public HaskellQvarOp getQvarOp() {
    return findChildByClass(HaskellQvarOp.class);
  }

  @Override
  @Nullable
  public HaskellTtype getTtype() {
    return findChildByClass(HaskellTtype.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignatureDeclaration.class);
  }

  public Seq<HaskellNamedElement> getIdentifierElements() {
    return HaskellPsiImplUtil.getIdentifierElements(this);
  }

}
