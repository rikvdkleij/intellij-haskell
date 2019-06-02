// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellImportDeclarations extends HaskellCompositeElement {

  @NotNull
  List<HaskellImportDeclaration> getImportDeclarationList();

  @NotNull
  List<HaskellPragma> getPragmaList();

}
