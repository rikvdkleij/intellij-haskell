// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellNewconstr extends HaskellCompositeElement {

  @Nullable
  HaskellGtycon getGtycon();

  @Nullable
  HaskellNewconstrFielddecl getNewconstrFielddecl();

  @Nullable
  HaskellQcon getQcon();

  @NotNull
  List<HaskellQvar> getQvarList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
