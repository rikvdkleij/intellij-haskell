// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCdecl extends HaskellCompositeElement {

  @Nullable
  HaskellDataDeclaration getDataDeclaration();

  @Nullable
  HaskellDefaultDeclaration getDefaultDeclaration();

  @Nullable
  HaskellInlinePragma getInlinePragma();

  @Nullable
  HaskellInstanceDeclaration getInstanceDeclaration();

  @Nullable
  HaskellMinimalPragma getMinimalPragma();

  @Nullable
  HaskellNewtypeDeclaration getNewtypeDeclaration();

  @Nullable
  HaskellNoinlinePragma getNoinlinePragma();

  @Nullable
  HaskellSpecializePragma getSpecializePragma();

  @Nullable
  HaskellTypeDeclaration getTypeDeclaration();

  @Nullable
  HaskellTypeSignatureDeclaration getTypeSignatureDeclaration();

}
