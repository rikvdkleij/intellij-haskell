// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellInst extends HaskellCompositeElement {

    @NotNull
    List<HaskellGtycon> getGtyconList();

    @NotNull
    List<HaskellInstvar> getInstvarList();

    @NotNull
    List<HaskellPragma> getPragmaList();

}
