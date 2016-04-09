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

public class HaskellQconOpImpl extends HaskellCompositeElementImpl implements HaskellQconOp {

  public HaskellQconOpImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQconOp(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellConId getConId() {
    return findChildByClass(HaskellConId.class);
  }

  @Override
  @Nullable
  public HaskellConSym getConSym() {
    return findChildByClass(HaskellConSym.class);
  }

  @Override
  @Nullable
  public HaskellGconSym getGconSym() {
    return findChildByClass(HaskellGconSym.class);
  }

  @Override
  @Nullable
  public HaskellQconId getQconId() {
    return findChildByClass(HaskellQconId.class);
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
