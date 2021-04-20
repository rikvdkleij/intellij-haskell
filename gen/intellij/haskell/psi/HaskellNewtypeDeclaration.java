// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.Option;
import scala.collection.immutable.Seq;

public interface HaskellNewtypeDeclaration extends HaskellTopDeclaration, HaskellDataConstructorDeclarationElement {

  @Nullable
  HaskellCcontext getCcontext();

  @NotNull
  List<HaskellDerivingVia> getDerivingViaList();

  @NotNull
  HaskellNewconstr getNewconstr();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  HaskellSimpletype getSimpletype();

  @Nullable
  HaskellTextLiteral getTextLiteral();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

  HaskellNamedElement getDataTypeConstructor();

}
