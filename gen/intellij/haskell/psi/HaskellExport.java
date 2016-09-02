// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellExport extends HaskellCompositeElement {

  @NotNull
  List<HaskellCname> getCnameList();

  @Nullable
  HaskellConid getConid();

  @Nullable
  HaskellDotDotParens getDotDotParens();

  @Nullable
  HaskellModid getModid();

  @Nullable
  HaskellQCon getQCon();

}
