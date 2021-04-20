// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTopDeclaration extends HaskellCompositeElement {

  @Nullable
  HaskellExpression getExpression();

  @Nullable
  HaskellPragma getPragma();

  @Nullable
  HaskellTopDeclaration getTopDeclaration();

  @Nullable
  HaskellTypeSignature getTypeSignature();

}
