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

public class HaskellIncludePragmaImpl extends HaskellCompositeElementImpl implements HaskellIncludePragma {

  public HaskellIncludePragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitIncludePragma(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGeneralPragmaContent getGeneralPragmaContent() {
    return findChildByClass(HaskellGeneralPragmaContent.class);
  }

}
