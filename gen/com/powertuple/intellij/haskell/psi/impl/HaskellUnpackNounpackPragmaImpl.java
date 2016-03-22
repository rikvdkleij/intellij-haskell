// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;
import com.powertuple.intellij.haskell.psi.*;

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
  public HaskellNounpackPragma getNounpackPragma() {
    return findChildByClass(HaskellNounpackPragma.class);
  }

  @Override
  @Nullable
  public HaskellUnpackPragma getUnpackPragma() {
    return findChildByClass(HaskellUnpackPragma.class);
  }

}
