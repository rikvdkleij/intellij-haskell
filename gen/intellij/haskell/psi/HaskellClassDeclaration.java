// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public interface HaskellClassDeclaration extends HaskellDeclarationElement {

  @NotNull
  List<HaskellCdecl> getCdeclList();

  @Nullable
  HaskellContext getContext();

  @NotNull
  List<HaskellExpression> getExpressionList();

  @NotNull
  HaskellQcon getQcon();

  @NotNull
  List<HaskellQvar> getQvarList();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

}
