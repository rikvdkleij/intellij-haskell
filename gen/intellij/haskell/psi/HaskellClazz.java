// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellClazz extends HaskellCompositeElement {

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTtype1> getTtype1List();

  @NotNull
  List<HaskellTtype2Onls> getTtype2OnlsList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
