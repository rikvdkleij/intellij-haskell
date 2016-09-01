// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.Seq;

import java.util.List;

public interface HaskellInstanceDeclaration extends HaskellDeclarationElement {

  @NotNull
  List<HaskellExpression> getExpressionList();

  @NotNull
  List<HaskellIdecl> getIdeclList();

  @NotNull
  HaskellInst getInst();

  @Nullable
  HaskellOverlapPragma getOverlapPragma();

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellVarCon> getVarConList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

}
