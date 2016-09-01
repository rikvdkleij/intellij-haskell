// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellQConQualifierImpl extends HaskellCompositeElementImpl implements HaskellQConQualifier {

  public HaskellQConQualifierImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitQConQualifier(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellQConQualifier1 getQConQualifier1() {
    return findChildByClass(HaskellQConQualifier1.class);
  }

  @Override
  @Nullable
  public HaskellQConQualifier2 getQConQualifier2() {
    return findChildByClass(HaskellQConQualifier2.class);
  }

  @Override
  @Nullable
  public HaskellQConQualifier3 getQConQualifier3() {
    return findChildByClass(HaskellQConQualifier3.class);
  }

  @Override
  @Nullable
  public HaskellQConQualifier4 getQConQualifier4() {
    return findChildByClass(HaskellQConQualifier4.class);
  }

}
