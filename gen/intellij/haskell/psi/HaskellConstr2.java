// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellConstr2 extends HaskellCompositeElement {

  @Nullable
  HaskellQName getQName();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList();

}
