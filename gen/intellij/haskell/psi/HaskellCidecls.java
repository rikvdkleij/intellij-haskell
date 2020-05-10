// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellCidecls extends HaskellCompositeElement {

  @NotNull
  List<HaskellCidecl> getCideclList();

  @NotNull
  List<HaskellPragma> getPragmaList();

}
