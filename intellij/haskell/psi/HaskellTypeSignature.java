// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTypeSignature extends HaskellDeclarationElement {

  @Nullable
  HaskellContext getContext();

  @Nullable
  HaskellFixity getFixity();

  @Nullable
  HaskellOps getOps();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @Nullable
  HaskellVars getVars();

}
