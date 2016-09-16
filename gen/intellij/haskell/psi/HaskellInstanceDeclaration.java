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

  @NotNull
  HaskellInst getInst();

  @Nullable
  HaskellOverlapPragma getOverlapPragma();

  @NotNull
  HaskellQName getQName();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellVarCon> getVarConList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

}
