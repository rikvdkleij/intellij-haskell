// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellGeneralPragmaContent extends HaskellCompositeElement {

  @NotNull
  List<HaskellFixity> getFixityList();

  @NotNull
  List<HaskellLiteral> getLiteralList();

  @NotNull
  List<HaskellQcon> getQconList();

  @NotNull
  List<HaskellQconOp> getQconOpList();

  @NotNull
  List<HaskellQvar> getQvarList();

  @NotNull
  List<HaskellQvarOp> getQvarOpList();

}
