// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellImportId extends HaskellCompositeElement {

  @NotNull
  List<HaskellCname> getCnameList();

  @Nullable
  HaskellDotDotParens getDotDotParens();

  @Nullable
  HaskellQcon getQcon();

  @Nullable
  HaskellQvar getQvar();

}
