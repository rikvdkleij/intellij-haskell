// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCdecls extends HaskellCompositeElement {

  @NotNull
  List<HaskellCdecl> getCdeclList();

  @NotNull
  List<HaskellPragma> getPragmaList();

}
