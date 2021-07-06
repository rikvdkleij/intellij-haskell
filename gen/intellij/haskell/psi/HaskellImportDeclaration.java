// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.Option;

public interface HaskellImportDeclaration extends HaskellTopDeclaration {

  @Nullable
  HaskellImportPackageName getImportPackageName();

  @Nullable
  HaskellImportQualified getImportQualified();

  @Nullable
  HaskellImportQualifiedAs getImportQualifiedAs();

  @Nullable
  HaskellImportSpec getImportSpec();

  @Nullable
  HaskellModid getModid();

  @NotNull
  List<HaskellPragma> getPragmaList();

  Option<String> getModuleName();

}
