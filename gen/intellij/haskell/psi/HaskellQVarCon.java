// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellQVarCon extends HaskellCompositeElement {

  @Nullable
  HaskellConsym getConsym();

  @Nullable
  HaskellQCon getQCon();

  @Nullable
  HaskellQualifier getQualifier();

  @Nullable
  HaskellVarid getVarid();

  @Nullable
  HaskellVarsym getVarsym();

  String getName();

  HaskellNamedElement getIdentifierElement();

}
