// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCidecl extends HaskellCompositeElement {

  @NotNull
  List<HaskellDotDot> getDotDotList();

  @Nullable
  HaskellPragma getPragma();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellQuasiQuote> getQuasiQuoteList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @Nullable
  HaskellTopDeclaration getTopDeclaration();

}
