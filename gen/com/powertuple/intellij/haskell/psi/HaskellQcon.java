// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.Option;

public interface HaskellQcon extends HaskellQVarConOpElement {

  @Nullable
  HaskellConId getConId();

  @Nullable
  HaskellConSym getConSym();

  @Nullable
  HaskellGconSym getGconSym();

  @Nullable
  HaskellQconId getQconId();

  String getName();

  HaskellNamedElement getIdentifierElement();

  Option<String> getQualifier();

}
