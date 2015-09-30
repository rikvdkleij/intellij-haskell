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

public class HaskellFileHeaderPragmaImpl extends HaskellCompositeElementImpl implements HaskellFileHeaderPragma {

  public HaskellFileHeaderPragmaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitFileHeaderPragma(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellAnnPragma getAnnPragma() {
    return findChildByClass(HaskellAnnPragma.class);
  }

  @Override
  @Nullable
  public HaskellDummyHeaderPragma getDummyHeaderPragma() {
    return findChildByClass(HaskellDummyHeaderPragma.class);
  }

  @Override
  @Nullable
  public HaskellHaddockPragma getHaddockPragma() {
    return findChildByClass(HaskellHaddockPragma.class);
  }

  @Override
  @Nullable
  public HaskellIncludePragma getIncludePragma() {
    return findChildByClass(HaskellIncludePragma.class);
  }

  @Override
  @Nullable
  public HaskellLanguagePragma getLanguagePragma() {
    return findChildByClass(HaskellLanguagePragma.class);
  }

  @Override
  @Nullable
  public HaskellOptionsGhcPragma getOptionsGhcPragma() {
    return findChildByClass(HaskellOptionsGhcPragma.class);
  }

}
