// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellNewconstr extends HaskellCompositeElement {

    @Nullable
    HaskellNewconstrFielddecl getNewconstrFielddecl();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

    @Nullable
    HaskellTtype getTtype();

}
