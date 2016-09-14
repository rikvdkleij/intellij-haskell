// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.Nullable;

public interface HaskellIdecl extends HaskellCompositeElement {

  @Nullable
  HaskellDataDeclaration getDataDeclaration();

  @Nullable
  HaskellDefaultDeclaration getDefaultDeclaration();

  @Nullable
  HaskellInlinePragma getInlinePragma();

  @Nullable
  HaskellInstanceDeclaration getInstanceDeclaration();

  @Nullable
  HaskellMinimalPragma getMinimalPragma();

  @Nullable
  HaskellNewtypeDeclaration getNewtypeDeclaration();

  @Nullable
  HaskellNoinlinePragma getNoinlinePragma();

  @Nullable
  HaskellSpecializePragma getSpecializePragma();

  @Nullable
  HaskellTypeDeclaration getTypeDeclaration();

  @Nullable
  HaskellTypeSignature getTypeSignature();

}
