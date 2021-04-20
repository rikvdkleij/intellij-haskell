// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTtype extends HaskellCompositeElement {

  @NotNull
  List<HaskellDerivingVia> getDerivingViaList();

  @Nullable
  HaskellListType getListType();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
