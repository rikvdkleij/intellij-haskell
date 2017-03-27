// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellFielddecl extends HaskellCompositeElement {

  @NotNull
  HaskellQNames getQNames();

  @Nullable
  HaskellScontext getScontext();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellUnpackNounpackPragma> getUnpackNounpackPragmaList();

}
