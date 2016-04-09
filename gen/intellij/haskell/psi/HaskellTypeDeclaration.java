// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public interface HaskellTypeDeclaration extends HaskellDeclarationElement {

  @Nullable
  HaskellExpression getExpression();

  @Nullable
  HaskellKindSignature getKindSignature();

  @NotNull
  HaskellSimpletype getSimpletype();

  @Nullable
  HaskellTtype getTtype();

  @Nullable
  HaskellTypeSignature getTypeSignature();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

}
