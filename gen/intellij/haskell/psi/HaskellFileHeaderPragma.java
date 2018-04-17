// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.Nullable;

public interface HaskellFileHeaderPragma extends HaskellCompositeElement {

  @Nullable
  HaskellAnnPragma getAnnPragma();

  @Nullable
  HaskellHaddockPragma getHaddockPragma();

  @Nullable
  HaskellIncludePragma getIncludePragma();

  @Nullable
  HaskellLanguagePragma getLanguagePragma();

  @Nullable
  HaskellOptionsGhcPragma getOptionsGhcPragma();

}
