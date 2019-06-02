// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import scala.collection.Seq;

public interface HaskellSimpletype extends HaskellCompositeElement {

  @NotNull
  List<HaskellQName> getQNameList();

  @Nullable
  HaskellTtype getTtype();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

  Seq<HaskellNamedElement> getIdentifierElements();

}
