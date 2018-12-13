// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.collection.Seq;

import java.util.List;

public interface HaskellSimpletype extends HaskellCompositeElement {

    @NotNull
    List<HaskellQName> getQNameList();

    @Nullable
    HaskellTtype getTtype();

    @NotNull
    List<HaskellTypeSignature> getTypeSignatureList();

    Seq<HaskellNamedElement> getIdentifierElements();

}
