// This is a generated file. Not intended for manual editing.
package intellij.haskell.alex.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AlexTokenSetDeclaration extends AlexElement {

  @NotNull
  List<AlexRegex> getRegexList();

  @NotNull
  AlexTokenSetId getTokenSetId();

}
