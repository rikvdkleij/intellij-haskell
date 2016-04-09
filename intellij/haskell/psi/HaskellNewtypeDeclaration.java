// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellNewtypeDeclaration extends HaskellDataConstructorDeclarationElement {

  @Nullable
  HaskellContext getContext();

  @Nullable
  HaskellCtypePragma getCtypePragma();

  @NotNull
  HaskellNewconstr getNewconstr();

  @NotNull
  HaskellSimpletype getSimpletype();

  @Nullable
  HaskellTtype getTtype();

}
