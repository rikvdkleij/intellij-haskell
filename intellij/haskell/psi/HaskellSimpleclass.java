// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellSimpleclass extends HaskellCompositeElement {

  @Nullable
  HaskellCname getCname();

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellQcon> getQconList();

  @NotNull
  List<HaskellQconOp> getQconOpList();

  @NotNull
  List<HaskellQvar> getQvarList();

  @NotNull
  List<HaskellSimpleclassTildePart> getSimpleclassTildePartList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
