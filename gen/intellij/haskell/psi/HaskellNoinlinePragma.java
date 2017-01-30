// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellNoinlinePragma extends HaskellCompositeElement {

  @NotNull
  List<HaskellInlinePragmas> getInlinePragmasList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellReservedId> getReservedIdList();

  @NotNull
  List<HaskellSccPragma> getSccPragmaList();

}
