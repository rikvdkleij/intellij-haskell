// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellDataDeclaration extends HaskellTopDeclaration, HaskellDataConstructorDeclarationElement {

    @Nullable
    HaskellCcontext getCcontext();

    @NotNull
    List<HaskellConstr> getConstrList();

    @Nullable
    HaskellDataDeclarationDeriving getDataDeclarationDeriving();

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

    @NotNull
    List<HaskellTypeSignature> getTypeSignatureList();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

    HaskellNamedElement getDataTypeConstructor();

}
