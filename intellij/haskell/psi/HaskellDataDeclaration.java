// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellDataDeclaration extends HaskellDataConstructorDeclarationElement {

  @NotNull
  List<HaskellCdecl> getCdeclList();

  @NotNull
  List<HaskellConstr1> getConstr1List();

  @NotNull
  List<HaskellConstr2> getConstr2List();

  @NotNull
  List<HaskellConstr3> getConstr3List();

  @NotNull
  List<HaskellConstr4> getConstr4List();

  @Nullable
  HaskellContext getContext();

  @Nullable
  HaskellCtypePragma getCtypePragma();

  @Nullable
  HaskellDataDeclarationDeriving getDataDeclarationDeriving();

  @NotNull
  List<HaskellExpression> getExpressionList();

  @Nullable
  HaskellKindSignature getKindSignature();

  @NotNull
  List<HaskellQvar> getQvarList();

  @NotNull
  List<HaskellSimpletype> getSimpletypeList();

}
