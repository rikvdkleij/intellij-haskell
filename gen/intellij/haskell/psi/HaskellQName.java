// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.Nullable;
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
