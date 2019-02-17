// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellImportDeclarations extends HaskellCompositeElement {

    @NotNull
    List<HaskellImportDeclaration> getImportDeclarationList();

    @NotNull
    List<HaskellPragma> getPragmaList();

}
