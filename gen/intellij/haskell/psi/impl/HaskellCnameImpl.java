// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

public class HaskellCnameImpl extends HaskellCompositeElementImpl implements HaskellCname {

  public HaskellCnameImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitCname(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellCon getCon() {
    return PsiTreeUtil.getChildOfType(this, HaskellCon.class);
  }

  @Override
  @Nullable
  public HaskellConop getConop() {
    return PsiTreeUtil.getChildOfType(this, HaskellConop.class);
  }

  @Override
  @Nullable
  public HaskellVar getVar() {
    return PsiTreeUtil.getChildOfType(this, HaskellVar.class);
  }

  @Override
  @Nullable
  public HaskellVarop getVarop() {
    return PsiTreeUtil.getChildOfType(this, HaskellVarop.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public HaskellNamedElement getIdentifierElement() {
    return HaskellPsiImplUtil.getIdentifierElement(this);
  }

  public Option<String> getQualifierName() {
    return HaskellPsiImplUtil.getQualifierName(this);
  }

  public String getNameWithoutParens() {
    return HaskellPsiImplUtil.getNameWithoutParens(this);
  }

}
