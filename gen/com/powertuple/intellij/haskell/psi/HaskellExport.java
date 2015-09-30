// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellExport extends HaskellCompositeElement {

  @NotNull
  List<HaskellCname> getCnameList();

  @Nullable
  HaskellDotDotParens getDotDotParens();

  @Nullable
  HaskellModId getModId();

  @Nullable
  HaskellQcon getQcon();

  @Nullable
  HaskellQvar getQvar();

  @Nullable
  HaskellQvarOp getQvarOp();

}
