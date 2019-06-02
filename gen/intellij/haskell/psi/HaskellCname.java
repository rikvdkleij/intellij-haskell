// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.Option;

public interface HaskellCname extends HaskellQualifiedNameElement {

  @Nullable
  HaskellCon getCon();

  @Nullable
  HaskellConop getConop();

  @Nullable
  HaskellVar getVar();

  @Nullable
  HaskellVarop getVarop();

  String getName();

  HaskellNamedElement getIdentifierElement();

  Option<String> getQualifierName();

}
