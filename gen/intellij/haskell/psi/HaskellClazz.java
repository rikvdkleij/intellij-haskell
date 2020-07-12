// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellClazz extends HaskellCompositeElement {

    @NotNull
    List<HaskellDerivingVia> getDerivingViaList();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

    @NotNull
    List<HaskellTextLiteral> getTextLiteralList();

    @Nullable
    HaskellTtype getTtype();

    @NotNull
    List<HaskellTtype1> getTtype1List();

    @NotNull
    List<HaskellTtype2> getTtype2List();

    @NotNull
    List<HaskellTypeSignature> getTypeSignatureList();

}
