// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellClazz extends HaskellCompositeElement {

  @NotNull
  List<HaskellDerivingVia> getDerivingViaList();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
