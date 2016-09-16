// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellInsideQqExpression extends HaskellCompositeElement {

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellQuasiQuote> getQuasiQuoteList();

  @NotNull
  List<HaskellSccPragma> getSccPragmaList();

}
