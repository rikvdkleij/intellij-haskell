// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.Seq;

public interface HaskellModuleDeclaration extends HaskellDeclarationElement {

  @Nullable
  HaskellDeprecatedWarnPragma getDeprecatedWarnPragma();

  @Nullable
  HaskellExports getExports();

  @NotNull
  HaskellModid getModid();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

}
