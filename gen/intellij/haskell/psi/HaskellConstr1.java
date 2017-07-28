// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellConstr1 extends HaskellCompositeElement {

  @NotNull
  List<HaskellFielddecl> getFielddeclList();

  @Nullable
  HaskellQName getQName();

  @Nullable
  HaskellUnpackNounpackPragma getUnpackNounpackPragma();

}
