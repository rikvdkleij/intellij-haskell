// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellCdecls extends HaskellCompositeElement {

  @NotNull
  List<HaskellCdecl> getCdeclList();

  @NotNull
  List<HaskellPragma> getPragmaList();

}
