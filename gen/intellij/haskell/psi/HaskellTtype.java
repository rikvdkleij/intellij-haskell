// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTtype extends HaskellCompositeElement {

  @Nullable
  HaskellListType getListType();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTtype1> getTtype1List();

  @NotNull
  List<HaskellTtype2> getTtype2List();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
