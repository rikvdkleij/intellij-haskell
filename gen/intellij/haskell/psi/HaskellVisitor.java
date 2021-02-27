// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellVisitor extends PsiElementVisitor {

    public void visitApplicationExpression(@NotNull HaskellApplicationExpression o) {
        visitExpression(o);
    }

    public void visitAtomExpression(@NotNull HaskellAtomExpression o) {
        visitExpression(o);
    }

    public void visitBracketExpression(@NotNull HaskellBracketExpression o) {
        visitExpression(o);
    }

    public void visitCaseClause(@NotNull HaskellCaseClause o) {
        visitCompositeElement(o);
    }

    public void visitCaseOfExpression(@NotNull HaskellCaseOfExpression o) {
        visitExpression(o);
    }

    public void visitCcontext(@NotNull HaskellCcontext o) {
        visitCompositeElement(o);
    }

    public void visitCdecl(@NotNull HaskellCdecl o) {
        visitCompositeElement(o);
    }

    public void visitCdeclDataDeclaration(@NotNull HaskellCdeclDataDeclaration o) {
        visitTopDeclaration(o);
    }

    public void visitCdecls(@NotNull HaskellCdecls o) {
        visitCompositeElement(o);
    }

    public void visitCidecl(@NotNull HaskellCidecl o) {
        visitCompositeElement(o);
    }

    public void visitCidecls(@NotNull HaskellCidecls o) {
        visitCompositeElement(o);
    }

    public void visitClassDeclaration(@NotNull HaskellClassDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitClazz(@NotNull HaskellClazz o) {
        visitCompositeElement(o);
    }

    public void visitComments(@NotNull HaskellComments o) {
        visitCompositeElement(o);
    }

    public void visitConid(@NotNull HaskellConid o) {
        visitNamedElement(o);
    }

    public void visitConstr(@NotNull HaskellConstr o) {
        visitCompositeElement(o);
    }

    public void visitConstr1(@NotNull HaskellConstr1 o) {
        visitCompositeElement(o);
    }

    public void visitConstr2(@NotNull HaskellConstr2 o) {
        visitCompositeElement(o);
    }

    public void visitConstr3(@NotNull HaskellConstr3 o) {
        visitCompositeElement(o);
    }

    public void visitConsym(@NotNull HaskellConsym o) {
        visitNamedElement(o);
    }

    public void visitDataDeclaration(@NotNull HaskellDataDeclaration o) {
        visitTopDeclaration(o);
        // visitDataConstructorDeclarationElement(o);
    }

    public void visitDataDeclarationDeriving(@NotNull HaskellDataDeclarationDeriving o) {
        visitCompositeElement(o);
    }

    public void visitDefaultDeclaration(@NotNull HaskellDefaultDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
        // visitDeclarationElement(o);
    }

    public void visitDerivingDeclaration(@NotNull HaskellDerivingDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitDerivingVia(@NotNull HaskellDerivingVia o) {
        visitCompositeElement(o);
    }

    public void visitDoNotationExpression(@NotNull HaskellDoNotationExpression o) {
        visitExpression(o);
    }

    public void visitDotDot(@NotNull HaskellDotDot o) {
        visitCompositeElement(o);
    }

    public void visitExport(@NotNull HaskellExport o) {
        visitCompositeElement(o);
    }

    public void visitExports(@NotNull HaskellExports o) {
        visitCompositeElement(o);
    }

    public void visitExpression(@NotNull HaskellExpression o) {
        visitExpressionElement(o);
    }

    public void visitFielddecl(@NotNull HaskellFielddecl o) {
        visitCompositeElement(o);
    }

    public void visitFileHeader(@NotNull HaskellFileHeader o) {
        visitCompositeElement(o);
    }

    public void visitFixityDeclaration(@NotNull HaskellFixityDeclaration o) {
        visitTopDeclaration(o);
    }

    public void visitForeignDeclaration(@NotNull HaskellForeignDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitGeneralPragmaContent(@NotNull HaskellGeneralPragmaContent o) {
        visitCompositeElement(o);
    }

    public void visitGtycon(@NotNull HaskellGtycon o) {
        visitCompositeElement(o);
    }

    public void visitIfExpression(@NotNull HaskellIfExpression o) {
        visitExpression(o);
    }

    public void visitImplementationDeclaration(@NotNull HaskellImplementationDeclaration o) {
        visitTopDeclaration(o);
    }

    public void visitImportDeclaration(@NotNull HaskellImportDeclaration o) {
        visitTopDeclaration(o);
    }

    public void visitImportDeclarations(@NotNull HaskellImportDeclarations o) {
        visitCompositeElement(o);
    }

    public void visitImportEmptySpec(@NotNull HaskellImportEmptySpec o) {
        visitCompositeElement(o);
    }

    public void visitImportHiding(@NotNull HaskellImportHiding o) {
        visitCompositeElement(o);
    }

    public void visitImportHidingSpec(@NotNull HaskellImportHidingSpec o) {
        visitCompositeElement(o);
    }

    public void visitImportId(@NotNull HaskellImportId o) {
        visitCompositeElement(o);
    }

    public void visitImportIdsSpec(@NotNull HaskellImportIdsSpec o) {
        visitCompositeElement(o);
    }

    public void visitImportPackageName(@NotNull HaskellImportPackageName o) {
        visitCompositeElement(o);
    }

    public void visitImportQualified(@NotNull HaskellImportQualified o) {
        visitCompositeElement(o);
    }

    public void visitImportQualifiedAs(@NotNull HaskellImportQualifiedAs o) {
        visitCompositeElement(o);
    }

    public void visitImportSpec(@NotNull HaskellImportSpec o) {
        visitCompositeElement(o);
    }

    public void visitInst(@NotNull HaskellInst o) {
        visitCompositeElement(o);
    }

    public void visitInstanceDeclaration(@NotNull HaskellInstanceDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitInstvar(@NotNull HaskellInstvar o) {
        visitCompositeElement(o);
    }

    public void visitKindSignature(@NotNull HaskellKindSignature o) {
        visitCompositeElement(o);
    }

    public void visitLetAbstraction(@NotNull HaskellLetAbstraction o) {
        visitExpression(o);
    }

    public void visitListType(@NotNull HaskellListType o) {
        visitCompositeElement(o);
    }

    public void visitModid(@NotNull HaskellModid o) {
        visitNamedElement(o);
    }

    public void visitModuleBody(@NotNull HaskellModuleBody o) {
        visitCompositeElement(o);
    }

    public void visitModuleDeclaration(@NotNull HaskellModuleDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitNewconstr(@NotNull HaskellNewconstr o) {
        visitCompositeElement(o);
    }

    public void visitNewconstrFielddecl(@NotNull HaskellNewconstrFielddecl o) {
        visitCompositeElement(o);
    }

    public void visitNewtypeDeclaration(@NotNull HaskellNewtypeDeclaration o) {
        visitTopDeclaration(o);
        // visitDataConstructorDeclarationElement(o);
    }

    public void visitParenExpression(@NotNull HaskellParenExpression o) {
        visitExpression(o);
    }

    public void visitPragma(@NotNull HaskellPragma o) {
        visitCompositeElement(o);
    }

    public void visitQCon(@NotNull HaskellQCon o) {
        visitCompositeElement(o);
    }

    public void visitQConQualifier(@NotNull HaskellQConQualifier o) {
        visitCompositeElement(o);
    }

    public void visitQConQualifier1(@NotNull HaskellQConQualifier1 o) {
        visitQualifierElement(o);
        // visitNamedElement(o);
    }

    public void visitQConQualifier2(@NotNull HaskellQConQualifier2 o) {
        visitQualifierElement(o);
        // visitNamedElement(o);
    }

    public void visitQConQualifier3(@NotNull HaskellQConQualifier3 o) {
        visitQualifierElement(o);
        // visitNamedElement(o);
    }

    public void visitQConQualifier4(@NotNull HaskellQConQualifier4 o) {
        visitQualifierElement(o);
        // visitNamedElement(o);
    }

    public void visitQName(@NotNull HaskellQName o) {
        visitQualifiedNameElement(o);
    }

    public void visitQNames(@NotNull HaskellQNames o) {
        visitCompositeElement(o);
    }

    public void visitQVarCon(@NotNull HaskellQVarCon o) {
        visitCompositeElement(o);
    }

    public void visitQualifier(@NotNull HaskellQualifier o) {
        visitQualifierElement(o);
        // visitNamedElement(o);
    }

    public void visitQuasiQuote(@NotNull HaskellQuasiQuote o) {
        visitQuasiQuoteElement(o);
        // visitPsiLanguageInjectionHost(o);
    }

    public void visitScontext(@NotNull HaskellScontext o) {
        visitCompositeElement(o);
    }

    public void visitShebangLine(@NotNull HaskellShebangLine o) {
        visitCompositeElement(o);
    }

    public void visitSimpleclass(@NotNull HaskellSimpleclass o) {
        visitCompositeElement(o);
    }

    public void visitSimpletype(@NotNull HaskellSimpletype o) {
        visitCompositeElement(o);
    }

    public void visitTextLiteral(@NotNull HaskellTextLiteral o) {
        visitStringLiteralElement(o);
        // visitPsiLanguageInjectionHost(o);
    }

    public void visitTopDeclaration(@NotNull HaskellTopDeclaration o) {
        visitCompositeElement(o);
    }

    public void visitTtype(@NotNull HaskellTtype o) {
        visitCompositeElement(o);
    }

    public void visitTypeDeclaration(@NotNull HaskellTypeDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitTypeEquality(@NotNull HaskellTypeEquality o) {
        visitCompositeElement(o);
    }

    public void visitTypeFamilyDeclaration(@NotNull HaskellTypeFamilyDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitTypeFamilyType(@NotNull HaskellTypeFamilyType o) {
        visitCompositeElement(o);
    }

    public void visitTypeInstanceDeclaration(@NotNull HaskellTypeInstanceDeclaration o) {
        visitTopDeclaration(o);
        // visitDeclarationElement(o);
    }

    public void visitTypeSignature(@NotNull HaskellTypeSignature o) {
        visitDeclarationElement(o);
    }

    public void visitVarCon(@NotNull HaskellVarCon o) {
        visitCompositeElement(o);
    }

    public void visitVarid(@NotNull HaskellVarid o) {
        visitNamedElement(o);
    }

    public void visitVarsym(@NotNull HaskellVarsym o) {
        visitNamedElement(o);
    }

    public void visitWhereClause(@NotNull HaskellWhereClause o) {
        visitCompositeElement(o);
    }

    public void visitDeclarationElement(@NotNull HaskellDeclarationElement o) {
        visitCompositeElement(o);
    }

    public void visitExpressionElement(@NotNull HaskellExpressionElement o) {
        visitCompositeElement(o);
    }

    public void visitNamedElement(@NotNull HaskellNamedElement o) {
        visitCompositeElement(o);
    }

    public void visitQualifiedNameElement(@NotNull HaskellQualifiedNameElement o) {
        visitCompositeElement(o);
    }

    public void visitQualifierElement(@NotNull HaskellQualifierElement o) {
        visitCompositeElement(o);
    }

    public void visitQuasiQuoteElement(@NotNull HaskellQuasiQuoteElement o) {
        visitCompositeElement(o);
    }

    public void visitStringLiteralElement(@NotNull HaskellStringLiteralElement o) {
        visitCompositeElement(o);
    }

    public void visitCompositeElement(@NotNull HaskellCompositeElement o) {
        visitElement(o);
    }

}
