// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

public class HaskellQNameImpl extends HaskellCompositeElementImpl implements HaskellQName {

  public HaskellQNameImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQName(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellQVarCon getQVarCon() {
    return findChildByClass(HaskellQVarCon.class);
  }

  @Override
  @Nullable
  public HaskellVarCon getVarCon() {
    return findChildByClass(HaskellVarCon.class);
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
