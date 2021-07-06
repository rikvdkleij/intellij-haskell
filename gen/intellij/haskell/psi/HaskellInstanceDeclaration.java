// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.Option;
import scala.collection.immutable.Seq;

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
