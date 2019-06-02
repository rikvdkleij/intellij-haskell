// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellFielddecl extends HaskellCompositeElement {

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  HaskellQNames getQNames();

  @Nullable
  HaskellScontext getScontext();

  @Nullable
  HaskellTtype getTtype();

}
