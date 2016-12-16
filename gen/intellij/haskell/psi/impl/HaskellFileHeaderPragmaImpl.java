// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellFileHeaderPragmaImpl extends HaskellCompositeElementImpl implements HaskellFileHeaderPragma {

  public HaskellFileHeaderPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitFileHeaderPragma(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellAnnPragma getAnnPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellAnnPragma.class);
  }

  @Override
  @Nullable
  public HaskellDummyPragma getDummyPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellDummyPragma.class);
  }

  @Override
  @Nullable
  public HaskellHaddockPragma getHaddockPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellHaddockPragma.class);
  }

  @Override
  @Nullable
  public HaskellIncludePragma getIncludePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellIncludePragma.class);
  }

  @Override
  @Nullable
  public HaskellLanguagePragma getLanguagePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellLanguagePragma.class);
  }

  @Override
  @Nullable
  public HaskellOptionsGhcPragma getOptionsGhcPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellOptionsGhcPragma.class);
  }

}
