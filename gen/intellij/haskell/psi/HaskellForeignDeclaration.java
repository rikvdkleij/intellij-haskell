// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import scala.Option;
import scala.collection.Seq;

public interface HaskellForeignDeclaration extends HaskellDeclarationElement {

  @NotNull
  HaskellExpression getExpression();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

}
