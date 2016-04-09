// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellSimpletype extends HaskellCompositeElement {

  @Nullable
  HaskellGconSym getGconSym();

  @Nullable
  HaskellParallelArrayType getParallelArrayType();

  @Nullable
  HaskellQcon getQcon();

  @Nullable
  HaskellQconOp getQconOp();

  @Nullable
  HaskellQvarOp getQvarOp();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  @NotNull
  List<HaskellVarId> getVarIdList();

}
