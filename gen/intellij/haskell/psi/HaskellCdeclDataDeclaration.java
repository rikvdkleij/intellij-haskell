// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellCdeclDataDeclaration extends HaskellCompositeElement {

    @NotNull
    List<HaskellKindSignature> getKindSignatureList();

    @Nullable
    HaskellPragma getPragma();

    @NotNull
    List<HaskellQName> getQNameList();

    @NotNull
    HaskellSimpletype getSimpletype();

    @Nullable
    HaskellTtype getTtype();

}
