// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.Option;
import scala.collection.Seq;

public interface HaskellClassDeclaration extends HaskellDeclarationElement {

  @Nullable
  HaskellCdecls getCdecls();

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellTtype> getTtypeList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

}
