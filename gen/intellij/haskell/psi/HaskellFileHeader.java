// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellFileHeader extends HaskellCompositeElement {

    @NotNull
    List<HaskellFileHeaderPragma> getFileHeaderPragmaList();

    @NotNull
    List<HaskellOptionsGhcPragma> getOptionsGhcPragmaList();

}
