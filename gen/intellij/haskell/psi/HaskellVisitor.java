// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLanguageInjectionHost;

public class HaskellVisitor extends PsiElementVisitor {

  public void visitCcontext(@NotNull HaskellCcontext o) {
    visitCompositeElement(o);
  }

  public void visitCdeclDataDeclaration(@NotNull HaskellCdeclDataDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitCdecls(@NotNull HaskellCdecls o) {
    visitCompositeElement(o);
  }

  public void visitCidecls(@NotNull HaskellCidecls o) {
    visitCompositeElement(o);
  }

  public void visitClassDeclaration(@NotNull HaskellClassDeclaration o) {
    visitDeclarationElement(o);
  }

  public void visitClazz(@NotNull HaskellClazz o) {
    visitCompositeElement(o);
  }

  public void visitCname(@NotNull HaskellCname o) {
    visitQualifiedNameElement(o);
  }

  public void visitCnameDotDot(@NotNull HaskellCnameDotDot o) {
    visitCompositeElement(o);
  }

  public void visitComments(@NotNull HaskellComments o) {
    visitCompositeElement(o);
  }

  public void visitCon(@NotNull HaskellCon o) {
    visitCNameElement(o);
  }

  public void visitConid(@NotNull HaskellConid o) {
    visitNamedElement(o);
  }

  public void visitConop(@NotNull HaskellConop o) {
    visitCNameElement(o);
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
    visitDataConstructorDeclarationElement(o);
  }

  public void visitDataDeclarationDeriving(@NotNull HaskellDataDeclarationDeriving o) {
    visitCompositeElement(o);
  }

  public void visitDefaultDeclaration(@NotNull HaskellDefaultDeclaration o) {
    visitDeclarationElement(o);
    // visitDeclarationElement(o);
  }

  public void visitDerivingDeclaration(@NotNull HaskellDerivingDeclaration o) {
    visitDeclarationElement(o);
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
    visitCompositeElement(o);
  }

  public void visitForeignDeclaration(@NotNull HaskellForeignDeclaration o) {
    visitDeclarationElement(o);
  }

  public void visitGeneralPragmaContent(@NotNull HaskellGeneralPragmaContent o) {
    visitCompositeElement(o);
  }

  public void visitGtycon(@NotNull HaskellGtycon o) {
    visitCompositeElement(o);
  }

  public void visitImportDeclaration(@NotNull HaskellImportDeclaration o) {
    visitCompositeElement(o);
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
    visitDeclarationElement(o);
  }

  public void visitInstvar(@NotNull HaskellInstvar o) {
    visitCompositeElement(o);
  }

  public void visitKindSignature(@NotNull HaskellKindSignature o) {
    visitCompositeElement(o);
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
    visitDeclarationElement(o);
  }

  public void visitNewconstr(@NotNull HaskellNewconstr o) {
    visitCompositeElement(o);
  }

  public void visitNewconstrFielddecl(@NotNull HaskellNewconstrFielddecl o) {
    visitCompositeElement(o);
  }

  public void visitNewtypeDeclaration(@NotNull HaskellNewtypeDeclaration o) {
    visitDataConstructorDeclarationElement(o);
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

  public void visitReservedId(@NotNull HaskellReservedId o) {
    visitCompositeElement(o);
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

  public void visitTopDeclarationLine(@NotNull HaskellTopDeclarationLine o) {
    visitCompositeElement(o);
  }

  public void visitTtype(@NotNull HaskellTtype o) {
    visitCompositeElement(o);
  }

  public void visitTtype1(@NotNull HaskellTtype1 o) {
    visitCompositeElement(o);
  }

  public void visitTtype2(@NotNull HaskellTtype2 o) {
    visitCompositeElement(o);
  }

  public void visitTypeDeclaration(@NotNull HaskellTypeDeclaration o) {
    visitDeclarationElement(o);
  }

  public void visitTypeEquality(@NotNull HaskellTypeEquality o) {
    visitCompositeElement(o);
  }

  public void visitTypeFamilyDeclaration(@NotNull HaskellTypeFamilyDeclaration o) {
    visitDeclarationElement(o);
  }

  public void visitTypeFamilyType(@NotNull HaskellTypeFamilyType o) {
    visitCompositeElement(o);
  }

  public void visitTypeInstanceDeclaration(@NotNull HaskellTypeInstanceDeclaration o) {
    visitDeclarationElement(o);
  }

  public void visitTypeSignature(@NotNull HaskellTypeSignature o) {
    visitDeclarationElement(o);
  }

  public void visitVar(@NotNull HaskellVar o) {
    visitCNameElement(o);
  }

  public void visitVarCon(@NotNull HaskellVarCon o) {
    visitCompositeElement(o);
  }

  public void visitVarid(@NotNull HaskellVarid o) {
    visitNamedElement(o);
  }

  public void visitVarop(@NotNull HaskellVarop o) {
    visitCNameElement(o);
  }

  public void visitVarsym(@NotNull HaskellVarsym o) {
    visitNamedElement(o);
  }

  public void visitCNameElement(@NotNull HaskellCNameElement o) {
    visitCompositeElement(o);
  }

  public void visitDataConstructorDeclarationElement(@NotNull HaskellDataConstructorDeclarationElement o) {
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

  public void visitStringLiteralElement(@NotNull HaskellStringLiteralElement o) {
    visitCompositeElement(o);
  }

  public void visitCompositeElement(@NotNull HaskellCompositeElement o) {
    visitElement(o);
  }

}
