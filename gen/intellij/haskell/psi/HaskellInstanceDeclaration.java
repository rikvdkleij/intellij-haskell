// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellInstanceDeclaration extends HaskellTopDeclaration, HaskellDeclarationElement {

    @Nullable
    HaskellCidecls getCidecls();

    @Nullable
    HaskellInst getInst();

    @NotNull
    List<HaskellPragma> getPragmaList();

    @Nullable
    HaskellQName getQName();

    @Nullable
    HaskellScontext getScontext();

    @Nullable
    HaskellTypeEquality getTypeEquality();

    @NotNull
    List<HaskellVarid> getVaridList();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

}
