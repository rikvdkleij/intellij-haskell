// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTypeFamilyType extends HaskellCompositeElement {

  @NotNull
  List<HaskellContext> getContextList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeFamilyType1> getTypeFamilyType1List();

  @NotNull
  List<HaskellTypeFamilyType2> getTypeFamilyType2List();

  @NotNull
  List<HaskellVars> getVarsList();

}
