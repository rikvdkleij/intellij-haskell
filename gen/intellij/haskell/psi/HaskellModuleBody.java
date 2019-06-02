// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellModuleBody extends HaskellCompositeElement {

  @NotNull
  HaskellImportDeclarations getImportDeclarations();

  @Nullable
  HaskellModuleDeclaration getModuleDeclaration();

  @Nullable
  HaskellTopDeclaration getTopDeclaration();

  @NotNull
  List<HaskellTopDeclarationLine> getTopDeclarationLineList();

}
