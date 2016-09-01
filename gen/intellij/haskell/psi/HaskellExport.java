// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellExport extends HaskellCompositeElement {

  @NotNull
  List<HaskellCname> getCnameList();

  @Nullable
  HaskellConid getConid();

  @Nullable
  HaskellDotDotParens getDotDotParens();

  @Nullable
  HaskellModid getModid();

  @Nullable
  HaskellQCon getQCon();

}
