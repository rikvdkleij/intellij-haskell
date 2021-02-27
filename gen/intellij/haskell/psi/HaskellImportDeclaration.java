// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

import java.util.List;

public interface HaskellImportDeclaration extends HaskellTopDeclaration {

    @Nullable
    HaskellImportPackageName getImportPackageName();

    @Nullable
    HaskellImportQualified getImportQualified();

    @Nullable
    HaskellImportQualifiedAs getImportQualifiedAs();

    @Nullable
    HaskellImportSpec getImportSpec();

    @Nullable
    HaskellModid getModid();

    @NotNull
    List<HaskellPragma> getPragmaList();

    Option<String> getModuleName();

}
