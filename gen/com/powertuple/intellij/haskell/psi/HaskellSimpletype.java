// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.collection.Seq;

public interface HaskellSimpletype extends HaskellCompositeElement {

  @Nullable
  HaskellGconSym getGconSym();

  @Nullable
  HaskellParallelArrayType getParallelArrayType();

  @Nullable
  HaskellQcon getQcon();

  @Nullable
  HaskellQvar getQvar();

  @Nullable
  HaskellQvarOp getQvarOp();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList();

  Seq<HaskellNamedElement> getIdentifierElements();

}
