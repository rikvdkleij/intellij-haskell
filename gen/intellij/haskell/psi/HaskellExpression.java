// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellExpression extends HaskellCompositeElement {

  @NotNull
  List<HaskellInlinePragmas> getInlinePragmasList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellReservedId> getReservedIdList();

  @NotNull
  List<HaskellSccPragma> getSccPragmaList();

  @NotNull
  List<HaskellTypeDeclaration> getTypeDeclarationList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
