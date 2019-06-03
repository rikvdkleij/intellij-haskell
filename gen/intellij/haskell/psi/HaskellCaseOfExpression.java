// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCaseOfExpression extends HaskellExpression {

  @NotNull
  List<HaskellCaseClause> getCaseClauseList();

  @Nullable
  HaskellExpression getExpression();

}
