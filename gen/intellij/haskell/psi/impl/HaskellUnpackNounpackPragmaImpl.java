// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellUnpackNounpackPragmaImpl extends HaskellCompositeElementImpl implements HaskellUnpackNounpackPragma {

  public HaskellUnpackNounpackPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitUnpackNounpackPragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellCtypePragma getCtypePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellCtypePragma.class);
  }

  @Override
  @Nullable
  public HaskellNounpackPragma getNounpackPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellNounpackPragma.class);
  }

  @Override
  @Nullable
  public HaskellUnpackPragma getUnpackPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellUnpackPragma.class);
  }

}
