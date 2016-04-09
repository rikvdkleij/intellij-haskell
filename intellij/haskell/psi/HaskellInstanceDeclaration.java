// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellInstanceDeclaration extends HaskellDeclarationElement {

  @NotNull
  List<HaskellExpression> getExpressionList();

  @NotNull
  List<HaskellIdecl> getIdeclList();

  @NotNull
  HaskellInst getInst();

  @Nullable
  HaskellOverlapPragma getOverlapPragma();

  @NotNull
  HaskellQcon getQcon();

  @Nullable
  HaskellScontext getScontext();

  @NotNull
  List<HaskellVarId> getVarIdList();

}
