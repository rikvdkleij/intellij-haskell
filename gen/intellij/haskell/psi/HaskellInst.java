// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellInst extends HaskellCompositeElement {

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellInstvar> getInstvarList();

  @Nullable
  HaskellQName getQName();

  @Nullable
  HaskellTtype getTtype();

}
