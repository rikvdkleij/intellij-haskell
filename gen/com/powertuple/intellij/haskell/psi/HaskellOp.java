// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellOp extends HaskellCompositeElement {

  @Nullable
  HaskellQconOp getQconOp();

  @Nullable
  HaskellQvarOp getQvarOp();

  String getName();

  HaskellNamedElement getIdentifierElement();

}
