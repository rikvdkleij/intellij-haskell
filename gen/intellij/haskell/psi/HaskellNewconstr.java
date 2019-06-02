// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellNewconstr extends HaskellCompositeElement {

  @Nullable
  HaskellNewconstrFielddecl getNewconstrFielddecl();

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellTextLiteral getTextLiteral();

  @NotNull
  List<HaskellTtype> getTtypeList();

  @Nullable
  HaskellTtype1 getTtype1();

  @Nullable
  HaskellTtype2 getTtype2();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
