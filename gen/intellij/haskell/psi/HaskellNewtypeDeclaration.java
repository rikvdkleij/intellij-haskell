// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public interface HaskellNewtypeDeclaration extends HaskellDataConstructorDeclarationElement {

  @Nullable
  HaskellCcontext getCcontext();

  @NotNull
  HaskellNewconstr getNewconstr();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  HaskellSimpletype getSimpletype();

  @Nullable
  HaskellTtype getTtype();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

  HaskellNamedElement getDataTypeConstructor();

}
