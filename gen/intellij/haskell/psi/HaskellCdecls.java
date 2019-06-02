// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellCdecls extends HaskellCompositeElement {

  @NotNull
  List<HaskellCdeclDataDeclaration> getCdeclDataDeclarationList();

  @NotNull
  List<HaskellDataDeclaration> getDataDeclarationList();

  @NotNull
  List<HaskellDefaultDeclaration> getDefaultDeclarationList();

  @NotNull
  List<HaskellDotDot> getDotDotList();

  @NotNull
  List<HaskellInstanceDeclaration> getInstanceDeclarationList();

  @NotNull
  List<HaskellNewtypeDeclaration> getNewtypeDeclarationList();

  @NotNull
  List<HaskellPragma> getPragmaList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellReservedId> getReservedIdList();

  @NotNull
  List<HaskellTextLiteral> getTextLiteralList();

  @NotNull
  List<HaskellTypeDeclaration> getTypeDeclarationList();

  @NotNull
  List<HaskellTypeFamilyDeclaration> getTypeFamilyDeclarationList();

  @NotNull
  List<HaskellTypeSignature> getTypeSignatureList();

}
