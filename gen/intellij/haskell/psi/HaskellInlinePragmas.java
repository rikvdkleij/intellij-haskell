// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellInlinePragmas extends HaskellCompositeElement {

  @Nullable
  HaskellInlineFusedPragma getInlineFusedPragma();

  @Nullable
  HaskellInlineInnerPragma getInlineInnerPragma();

  @Nullable
  HaskellInlinePragma getInlinePragma();

  @Nullable
  HaskellNoinlinePragma getNoinlinePragma();

}
