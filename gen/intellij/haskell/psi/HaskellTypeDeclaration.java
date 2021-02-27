// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellTypeDeclaration extends HaskellTopDeclaration, HaskellDeclarationElement {

    @NotNull
    List<HaskellKindSignature> getKindSignatureList();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

    @NotNull
    HaskellSimpletype getSimpletype();

    @Nullable
    HaskellTtype getTtype();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

}
