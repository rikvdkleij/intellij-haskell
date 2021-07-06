// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCidecls extends HaskellCompositeElement {

  @NotNull
  List<HaskellCidecl> getCideclList();

  @NotNull
  List<HaskellPragma> getPragmaList();

}
