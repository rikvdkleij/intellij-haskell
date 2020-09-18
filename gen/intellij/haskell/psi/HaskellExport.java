// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellExport extends HaskellCompositeElement {

    @Nullable
    HaskellDotDot getDotDot();

    @Nullable
    HaskellModid getModid();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

}
