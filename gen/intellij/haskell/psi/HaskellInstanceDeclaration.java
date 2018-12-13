// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.Seq;

import java.util.List;

public interface HaskellInstanceDeclaration extends HaskellDeclarationElement {

    @Nullable
    HaskellCidecls getCidecls();

    @Nullable
    HaskellIncoherentPragma getIncoherentPragma();

    @Nullable
    HaskellInst getInst();

    @Nullable
    HaskellOverlapPragma getOverlapPragma();

    @Nullable
    HaskellQName getQName();

    @Nullable
    HaskellScontext getScontext();

    @Nullable
    HaskellTypeEquality getTypeEquality();

    @NotNull
    List<HaskellVarCon> getVarConList();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

}
