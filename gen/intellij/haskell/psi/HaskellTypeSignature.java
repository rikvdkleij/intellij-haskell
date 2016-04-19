// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public interface HaskellTypeSignature extends HaskellDeclarationElement {

  @Nullable
  HaskellContext getContext();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @Nullable
  HaskellVarId getVarId();

  @NotNull
  HaskellVars getVars();

  String getName();

  ItemPresentation getPresentation();

  Seq<HaskellNamedElement> getIdentifierElements();

}
