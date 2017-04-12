// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.Seq;

import java.util.List;

public interface HaskellDataDeclaration extends HaskellDataConstructorDeclarationElement {

  @Nullable
  HaskellCcontext getCcontext();

  @NotNull
  List<HaskellConstr1> getConstr1List();

  @NotNull
  List<HaskellConstr2> getConstr2List();

  @NotNull
  List<HaskellConstr3> getConstr3List();

  @NotNull
  List<HaskellConstr4> getConstr4List();

  @Nullable
  HaskellCtypePragma getCtypePragma();

  @Nullable
  HaskellDataDeclarationDeriving getDataDeclarationDeriving();

  @NotNull
  List<HaskellKindSignature> getKindSignatureList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellSimpletype> getSimpletypeList();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

  Option<String> getModuleName();

  HaskellNamedElement getDataTypeConstructor();

}
