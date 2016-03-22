// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellFielddecl extends HaskellCompositeElement {

  @Nullable
  HaskellGtycon getGtycon();

  @NotNull
  List<HaskellQvar> getQvarList();

  @NotNull
  List<HaskellQvarOp> getQvarOpList();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @NotNull
  List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList();

  @Nullable
  HaskellUnpackNounpackPragma getUnpackNounpackPragma();

  @NotNull
  HaskellVars getVars();

}
