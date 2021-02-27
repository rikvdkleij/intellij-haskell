// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellTypeFamilyDeclaration extends HaskellTopDeclaration, HaskellDeclarationElement {

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    HaskellTypeFamilyType getTypeFamilyType();

    String getName();

    ItemPresentation getPresentation();

    Seq<HaskellNamedElement> getIdentifierElements();

    Option<String> getModuleName();

}
