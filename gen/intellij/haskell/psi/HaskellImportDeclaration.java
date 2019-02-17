// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.Nullable;
import scala.Option;

public interface HaskellImportDeclaration extends HaskellCompositeElement {

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

    @Nullable
    HaskellPragma getPragma();

    Option<String> getModuleName();

}
