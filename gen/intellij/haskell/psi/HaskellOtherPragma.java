// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellOtherPragma extends HaskellCompositeElement {

  @Nullable
  HaskellAnnPragma getAnnPragma();

  @Nullable
  HaskellDeprecatedWarnPragma getDeprecatedWarnPragma();

  @Nullable
  HaskellDummyPragma getDummyPragma();

  @Nullable
  HaskellInlinablePragma getInlinablePragma();

  @Nullable
  HaskellInlinePragma getInlinePragma();

  @Nullable
  HaskellLinePragma getLinePragma();

  @Nullable
  HaskellMinimalPragma getMinimalPragma();

  @Nullable
  HaskellNoinlinePragma getNoinlinePragma();

  @Nullable
  HaskellRulesPragma getRulesPragma();

  @Nullable
  HaskellSpecializePragma getSpecializePragma();

}
