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

public class HaskellModIdImpl extends HaskellNamedElementImpl implements HaskellModId {

  public HaskellModIdImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitModId(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  public PsiElement setName(String newName) {
    return HaskellPsiImplUtil.setName(this, newName);
  }

  public HaskellNamedElement getNameIdentifier() {
    return HaskellPsiImplUtil.getNameIdentifier(this);
  }

  public com.intellij.psi.PsiReference getReference() {
    return HaskellPsiImplUtil.getReference(this);
  }

  public com.intellij.navigation.ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  public com.intellij.psi.search.SearchScope getUseScope() {
    return HaskellPsiImplUtil.getUseScope(this);
  }

}
