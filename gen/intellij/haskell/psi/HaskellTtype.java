// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellTtype extends HaskellCompositeElement {

    @NotNull
    List<HaskellDerivingVia> getDerivingViaList();

    @Nullable
    HaskellListType getListType();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTtype1> getTtype1List();

  @NotNull
  List<HaskellTtype2> getTtype2List();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
