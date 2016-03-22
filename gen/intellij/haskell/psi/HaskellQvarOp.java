// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.Option;

public interface HaskellQvarOp extends HaskellQVarConOpElement {

  @Nullable
  HaskellQvarDotSym getQvarDotSym();

  @Nullable
  HaskellQvarId getQvarId();

  @Nullable
  HaskellQvarSym getQvarSym();

  @Nullable
  HaskellVarDotSym getVarDotSym();

  @Nullable
  HaskellVarId getVarId();

  @Nullable
  HaskellVarSym getVarSym();

  String getName();

  HaskellNamedElement getIdentifierElement();

  Option<String> getQualifier();

}
