// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellModuleDeclaration extends HaskellTopDeclaration, HaskellDeclarationElement {

    @Nullable
    HaskellExports getExports();

    @NotNull
    HaskellModid getModid();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    HaskellWhereClause getWhereClause();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

}
