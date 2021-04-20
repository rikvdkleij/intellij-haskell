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

public class HaskellCdeclImpl extends HaskellCompositeElementImpl implements HaskellCdecl {

  public HaskellCdeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitCdecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellCdeclDataDeclaration getCdeclDataDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellCdeclDataDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellCidecl getCidecl() {
    return PsiTreeUtil.getChildOfType(this, HaskellCidecl.class);
  }

  @Override
  @Nullable
  public HaskellTypeSignature getTypeSignature() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeSignature.class);
  }

}
