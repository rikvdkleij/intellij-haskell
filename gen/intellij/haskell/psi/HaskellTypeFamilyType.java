// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTypeFamilyType extends HaskellCompositeElement {

  @NotNull
  List<HaskellCcontext> getCcontextList();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellQNames> getQNamesList();

  @NotNull
  List<HaskellTtype> getTtypeList();

}
