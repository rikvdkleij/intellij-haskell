// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellNewconstr extends HaskellCompositeElement {

  @Nullable
  HaskellNewconstrFielddecl getNewconstrFielddecl();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellTtype getTtype();

}
