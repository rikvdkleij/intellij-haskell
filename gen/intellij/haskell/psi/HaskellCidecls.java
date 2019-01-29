// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellCidecls extends HaskellCompositeElement {

    @NotNull
    List<HaskellDataDeclaration> getDataDeclarationList();

    @NotNull
    List<HaskellDefaultDeclaration> getDefaultDeclarationList();

    @NotNull
    List<HaskellDotDot> getDotDotList();

    @NotNull
    List<HaskellInlinelikePragma> getInlinelikePragmaList();

    @NotNull
    List<HaskellInstanceDeclaration> getInstanceDeclarationList();

    @NotNull
    List<HaskellMinimalPragma> getMinimalPragmaList();

    @NotNull
    List<HaskellNewtypeDeclaration> getNewtypeDeclarationList();

    @NotNull
    List<HaskellQName> getQNameList();

    @NotNull
    List<HaskellReservedId> getReservedIdList();

    @NotNull
    List<HaskellSccPragma> getSccPragmaList();

    @NotNull
    List<HaskellSpecializePragma> getSpecializePragmaList();

    @NotNull
    List<HaskellTextLiteral> getTextLiteralList();

    @NotNull
    List<HaskellTypeDeclaration> getTypeDeclarationList();

    @NotNull
    List<HaskellTypeFamilyDeclaration> getTypeFamilyDeclarationList();

}
