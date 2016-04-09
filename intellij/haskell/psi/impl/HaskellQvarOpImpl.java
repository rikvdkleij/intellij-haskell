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

public class HaskellQvarOpImpl extends HaskellCompositeElementImpl implements HaskellQvarOp {

  public HaskellQvarOpImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQvarOp(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
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
