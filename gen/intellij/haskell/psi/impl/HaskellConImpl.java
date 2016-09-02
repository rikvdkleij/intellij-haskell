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

public class HaskellConImpl extends HaskellCNameElementImpl implements HaskellCon {

  public HaskellConImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitCon(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellConid getConid() {
    return findChildByClass(HaskellConid.class);
  }

  @Override
  @Nullable
  public HaskellConsym getConsym() {
    return findChildByClass(HaskellConsym.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

}
