// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.Option;

public interface HaskellQName extends HaskellQualifiedNameElement {

  @Nullable
  HaskellQVarCon getQVarCon();

  @Nullable
  HaskellVarCon getVarCon();

  String getName();

  HaskellNamedElement getIdentifierElement();

  Option<String> getQualifierName();

}
