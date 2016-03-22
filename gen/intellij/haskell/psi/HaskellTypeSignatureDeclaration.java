// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public interface HaskellTypeSignatureDeclaration extends HaskellDeclarationElement {

  @Nullable
  HaskellContext getContext();

  @Nullable
  HaskellFixity getFixity();

  @Nullable
  HaskellOps getOps();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @Nullable
  HaskellVars getVars();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

}
