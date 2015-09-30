// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellConstr4 extends HaskellCompositeElement {

  @NotNull
  HaskellGconSym getGconSym();

  @NotNull
  HaskellQcon getQcon();

  @NotNull
  HaskellQvar getQvar();

  @NotNull
  List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList();

}
