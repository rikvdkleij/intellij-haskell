// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellImportDeclaration extends HaskellCompositeElement {

  @NotNull
  HaskellImportModule getImportModule();

  @Nullable
  HaskellImportQualified getImportQualified();

  @Nullable
  HaskellImportQualifiedAs getImportQualifiedAs();

  @Nullable
  HaskellImportSpec getImportSpec();

  @Nullable
  HaskellSourcePragma getSourcePragma();

  String getModuleName();

}
