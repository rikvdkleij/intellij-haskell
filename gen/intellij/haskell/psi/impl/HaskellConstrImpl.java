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

public class HaskellConstrImpl extends HaskellCompositeElementImpl implements HaskellConstr {

  public HaskellConstrImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitConstr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellConstr1 getConstr1() {
    return PsiTreeUtil.getChildOfType(this, HaskellConstr1.class);
  }

  @Override
  @Nullable
  public HaskellConstr2 getConstr2() {
    return PsiTreeUtil.getChildOfType(this, HaskellConstr2.class);
  }

  @Override
  @Nullable
  public HaskellConstr3 getConstr3() {
    return PsiTreeUtil.getChildOfType(this, HaskellConstr3.class);
  }

  @Override
  @Nullable
  public HaskellTypeSignature getTypeSignature() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeSignature.class);
  }

}
