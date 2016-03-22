// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellFileHeaderPragma extends HaskellCompositeElement {

  @Nullable
  HaskellAnnPragma getAnnPragma();

  @Nullable
  HaskellDummyPragma getDummyPragma();

  @Nullable
  HaskellHaddockPragma getHaddockPragma();

  @Nullable
  HaskellIncludePragma getIncludePragma();

  @Nullable
  HaskellLanguagePragma getLanguagePragma();

  @Nullable
  HaskellOptionsGhcPragma getOptionsGhcPragma();

}
