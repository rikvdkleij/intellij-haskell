// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellSimpleclass extends HaskellCompositeElement {

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellQcon> getQconList();

  @NotNull
  List<HaskellQvar> getQvarList();

  @Nullable
  HaskellQvarOp getQvarOp();

  @NotNull
  List<HaskellSimpleclassTildePart> getSimpleclassTildePartList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList();

}
