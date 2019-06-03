// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellWhereClause extends HaskellCompositeElement {

  @Nullable
  HaskellImportDeclarations getImportDeclarations();

  @Nullable
  HaskellTopDeclaration getTopDeclaration();

  @NotNull
  List<HaskellTopDeclarationLine> getTopDeclarationLineList();

}
