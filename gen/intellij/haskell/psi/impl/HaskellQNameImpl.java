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
    return PsiTreeUtil.getChildOfType(this, HaskellQVarCon.class);
  }

  @Override
  @Nullable
  public HaskellVarCon getVarCon() {
    return PsiTreeUtil.getChildOfType(this, HaskellVarCon.class);
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
