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

public class HaskellTypeDeclarationImpl extends HaskellCompositeElementImpl implements HaskellTypeDeclaration {

  public HaskellTypeDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTypeDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellSimpletype getSimpletype() {
    return findNotNullChildByClass(HaskellSimpletype.class);
  }

  @Override
  @Nullable
  public HaskellTtype getTtype() {
    return findChildByClass(HaskellTtype.class);
  }

  @Override
  @Nullable
  public HaskellTypeSignatureDeclaration getTypeSignatureDeclaration() {
    return findChildByClass(HaskellTypeSignatureDeclaration.class);
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
