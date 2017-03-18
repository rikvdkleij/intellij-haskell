// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellExport extends HaskellCompositeElement {

  @Nullable
  HaskellCname getCname();

  @NotNull
  List<HaskellCnameDotDot> getCnameDotDotList();

  @Nullable
  HaskellModid getModid();

  @Nullable
  HaskellQCon getQCon();

}
