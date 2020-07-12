// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellNewconstr extends HaskellCompositeElement {

    @NotNull
    List<HaskellDerivingVia> getDerivingViaList();

    @Nullable
    HaskellNewconstrFielddecl getNewconstrFielddecl();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

    @Nullable
    HaskellTextLiteral getTextLiteral();

    @NotNull
    List<HaskellTtype> getTtypeList();

    @Nullable
    HaskellTtype1 getTtype1();

    @Nullable
    HaskellTtype2 getTtype2();

    @NotNull
    List<HaskellTypeSignature> getTypeSignatureList();

}
