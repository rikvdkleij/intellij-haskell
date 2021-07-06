// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static intellij.haskell.psi.HaskellTypes.*;
import intellij.haskell.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;

public class HaskellQConQualifier4Impl extends HaskellQualifierElementImpl implements HaskellQConQualifier4 {

  public HaskellQConQualifier4Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQConQualifier4(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellConid> getConidList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConid.class);
  }

  @Override
  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String newName) {
    return HaskellPsiImplUtil.setName(this, newName);
  }

  @Override
  public HaskellNamedElement getNameIdentifier() {
    return HaskellPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public PsiReference getReference() {
    return HaskellPsiImplUtil.getReference(this);
  }

  @Override
  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

}
