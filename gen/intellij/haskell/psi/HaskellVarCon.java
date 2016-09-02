// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellVarCon extends HaskellCompositeElement {

  @Nullable
  HaskellConid getConid();

  @Nullable
  HaskellConsym getConsym();

  @Nullable
  HaskellVarid getVarid();

  @Nullable
  HaskellVarsym getVarsym();

  String getName();

  HaskellNamedElement getIdentifierElement();

}
