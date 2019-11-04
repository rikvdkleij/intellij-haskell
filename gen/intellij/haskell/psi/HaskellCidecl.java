// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellCidecl extends HaskellCompositeElement {

  @Nullable
  HaskellDataDeclaration getDataDeclaration();

  @Nullable
  HaskellDefaultDeclaration getDefaultDeclaration();

  @NotNull
  List<HaskellDotDot> getDotDotList();

  @Nullable
  HaskellInstanceDeclaration getInstanceDeclaration();

  @Nullable
  HaskellNewtypeDeclaration getNewtypeDeclaration();

  @Nullable
  HaskellPragma getPragma();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellQuasiQuote> getQuasiQuoteList();

  @NotNull
  List<HaskellReservedId> getReservedIdList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @Nullable
  HaskellTypeDeclaration getTypeDeclaration();

  @Nullable
  HaskellTypeFamilyDeclaration getTypeFamilyDeclaration();

}
