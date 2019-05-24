// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellWhereClause extends HaskellCompositeElement {

    @Nullable
    HaskellImportDeclarations getImportDeclarations();

    @Nullable
    HaskellTopDeclaration getTopDeclaration();

  @NotNull
  List<HaskellTopDeclarationLine> getTopDeclarationLineList();

}
