// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellFielddecl extends HaskellCompositeElement {

  @Nullable
  HaskellCname getCname();

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellQvar> getQvarList();

  @Nullable
  HaskellQvarOp getQvarOp();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  @Nullable
  HaskellUnpackNounpackPragma getUnpackNounpackPragma();

  @NotNull
  HaskellVars getVars();

}
