// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellInst extends HaskellCompositeElement {

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellInstvar> getInstvarList();

  @Nullable
  HaskellQvar getQvar();

}
