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

public class HaskellImportDeclarationImpl extends HaskellCompositeElementImpl implements HaskellImportDeclaration {

  public HaskellImportDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) ((HaskellVisitor)visitor).visitImportDeclaration(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellImportModule getImportModule() {
    return findNotNullChildByClass(HaskellImportModule.class);
  }

  @Override
  @Nullable
  public HaskellImportQualified getImportQualified() {
    return findChildByClass(HaskellImportQualified.class);
  }

  @Override
  @Nullable
  public HaskellImportQualifiedAs getImportQualifiedAs() {
    return findChildByClass(HaskellImportQualifiedAs.class);
  }

  @Override
  @Nullable
  public HaskellImportSpec getImportSpec() {
    return findChildByClass(HaskellImportSpec.class);
  }

  @Override
  @Nullable
  public HaskellSourcePragma getSourcePragma() {
    return findChildByClass(HaskellSourcePragma.class);
  }

  public String getModuleName() {
    return HaskellPsiImplUtil.getModuleName(this);
  }

}
