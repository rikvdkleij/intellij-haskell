// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public interface HaskellDefaultDeclaration extends HaskellDeclarationElement {

  @NotNull
  List<HaskellTtype> getTtypeList();

  @Nullable
  HaskellTypeSignatureDeclaration getTypeSignatureDeclaration();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  String getModuleName();

}
