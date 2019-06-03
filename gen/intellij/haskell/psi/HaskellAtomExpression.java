// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellAtomExpression extends HaskellExpression {

  @Nullable
  HaskellDotDot getDotDot();

  @Nullable
  HaskellPragma getPragma();

  @Nullable
  HaskellQName getQName();

  @Nullable
  HaskellReservedId getReservedId();

  @Nullable
  HaskellTextLiteral getTextLiteral();

}
