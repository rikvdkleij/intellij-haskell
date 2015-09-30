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
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public class HaskellTypeSignatureDeclarationImpl extends HaskellCompositeElementImpl implements HaskellTypeSignatureDeclaration {

  public HaskellTypeSignatureDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitTypeSignatureDeclaration(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellContext getContext() {
    return findChildByClass(HaskellContext.class);
  }

  @Override
  @Nullable
  public HaskellFixity getFixity() {
    return findChildByClass(HaskellFixity.class);
  }

  @Override
  @Nullable
  public HaskellOps getOps() {
    return findChildByClass(HaskellOps.class);
  }

  @Override
  @NotNull
  public List<HaskellTtype> getTtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
  }

  @Override
  @Nullable
  public HaskellVars getVars() {
    return findChildByClass(HaskellVars.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  public Seq<HaskellNamedElement> getIdentifierElements() {
    return HaskellPsiImplUtil.getIdentifierElements(this);
  }

}
