// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import intellij.haskell.psi.impl.*;

public interface HaskellTypes {

    IElementType HS_APPLICATION_EXPRESSION = new HaskellCompositeElementType("HS_APPLICATION_EXPRESSION");
    IElementType HS_ATOM_EXPRESSION = new HaskellCompositeElementType("HS_ATOM_EXPRESSION");
    IElementType HS_BRACKET_EXPRESSION = new HaskellCompositeElementType("HS_BRACKET_EXPRESSION");
    IElementType HS_CASE_CLAUSE = new HaskellCompositeElementType("HS_CASE_CLAUSE");
    IElementType HS_CASE_OF_EXPRESSION = new HaskellCompositeElementType("HS_CASE_OF_EXPRESSION");
    IElementType HS_CCONTEXT = new HaskellCompositeElementType("HS_CCONTEXT");
    IElementType HS_CDECL = new HaskellCompositeElementType("HS_CDECL");
    IElementType HS_CDECLS = new HaskellCompositeElementType("HS_CDECLS");
    IElementType HS_CDECL_DATA_DECLARATION = new HaskellCompositeElementType("HS_CDECL_DATA_DECLARATION");
    IElementType HS_CIDECL = new HaskellCompositeElementType("HS_CIDECL");
    IElementType HS_CIDECLS = new HaskellCompositeElementType("HS_CIDECLS");
    IElementType HS_CLASS_DECLARATION = new HaskellCompositeElementType("HS_CLASS_DECLARATION");
    IElementType HS_CLAZZ = new HaskellCompositeElementType("HS_CLAZZ");
    IElementType HS_COMMENTS = new HaskellCompositeElementType("HS_COMMENTS");
    IElementType HS_CONID = HaskellElementTypeFactory.factory("HS_CONID");
    IElementType HS_CONSTR = new HaskellCompositeElementType("HS_CONSTR");
    IElementType HS_CONSTR_1 = new HaskellCompositeElementType("HS_CONSTR_1");
    IElementType HS_CONSTR_2 = new HaskellCompositeElementType("HS_CONSTR_2");
    IElementType HS_CONSTR_3 = new HaskellCompositeElementType("HS_CONSTR_3");
    IElementType HS_CONSYM = HaskellElementTypeFactory.factory("HS_CONSYM");
    IElementType HS_DATA_DECLARATION = new HaskellCompositeElementType("HS_DATA_DECLARATION");
    IElementType HS_DATA_DECLARATION_DERIVING = new HaskellCompositeElementType("HS_DATA_DECLARATION_DERIVING");
    IElementType HS_DEFAULT_DECLARATION = new HaskellCompositeElementType("HS_DEFAULT_DECLARATION");
    IElementType HS_DERIVING_DECLARATION = new HaskellCompositeElementType("HS_DERIVING_DECLARATION");
    IElementType HS_DERIVING_VIA = new HaskellCompositeElementType("HS_DERIVING_VIA");
    IElementType HS_DOT_DOT = new HaskellCompositeElementType("HS_DOT_DOT");
    IElementType HS_DO_NOTATION_EXPRESSION = new HaskellCompositeElementType("HS_DO_NOTATION_EXPRESSION");
    IElementType HS_EXPORT = new HaskellCompositeElementType("HS_EXPORT");
    IElementType HS_EXPORTS = new HaskellCompositeElementType("HS_EXPORTS");
    IElementType HS_EXPRESSION = new HaskellCompositeElementType("HS_EXPRESSION");
    IElementType HS_FIELDDECL = new HaskellCompositeElementType("HS_FIELDDECL");
    IElementType HS_FILE_HEADER = new HaskellCompositeElementType("HS_FILE_HEADER");
    IElementType HS_FIXITY_DECLARATION = new HaskellCompositeElementType("HS_FIXITY_DECLARATION");
    IElementType HS_FOREIGN_DECLARATION = new HaskellCompositeElementType("HS_FOREIGN_DECLARATION");
    IElementType HS_GENERAL_PRAGMA_CONTENT = new HaskellCompositeElementType("HS_GENERAL_PRAGMA_CONTENT");
    IElementType HS_GTYCON = new HaskellCompositeElementType("HS_GTYCON");
    IElementType HS_IF_EXPRESSION = new HaskellCompositeElementType("HS_IF_EXPRESSION");
    IElementType HS_IMPLEMENTATION_DECLARATION = new HaskellCompositeElementType("HS_IMPLEMENTATION_DECLARATION");
    IElementType HS_IMPORT_DECLARATION = new HaskellCompositeElementType("HS_IMPORT_DECLARATION");
    IElementType HS_IMPORT_DECLARATIONS = new HaskellCompositeElementType("HS_IMPORT_DECLARATIONS");
    IElementType HS_IMPORT_EMPTY_SPEC = new HaskellCompositeElementType("HS_IMPORT_EMPTY_SPEC");
    IElementType HS_IMPORT_HIDING = new HaskellCompositeElementType("HS_IMPORT_HIDING");
    IElementType HS_IMPORT_HIDING_SPEC = new HaskellCompositeElementType("HS_IMPORT_HIDING_SPEC");
    IElementType HS_IMPORT_ID = new HaskellCompositeElementType("HS_IMPORT_ID");
    IElementType HS_IMPORT_IDS_SPEC = new HaskellCompositeElementType("HS_IMPORT_IDS_SPEC");
    IElementType HS_IMPORT_PACKAGE_NAME = new HaskellCompositeElementType("HS_IMPORT_PACKAGE_NAME");
    IElementType HS_IMPORT_QUALIFIED = new HaskellCompositeElementType("HS_IMPORT_QUALIFIED");
    IElementType HS_IMPORT_QUALIFIED_AS = new HaskellCompositeElementType("HS_IMPORT_QUALIFIED_AS");
    IElementType HS_IMPORT_SPEC = new HaskellCompositeElementType("HS_IMPORT_SPEC");
    IElementType HS_INST = new HaskellCompositeElementType("HS_INST");
    IElementType HS_INSTANCE_DECLARATION = new HaskellCompositeElementType("HS_INSTANCE_DECLARATION");
    IElementType HS_INSTVAR = new HaskellCompositeElementType("HS_INSTVAR");
    IElementType HS_KIND_SIGNATURE = new HaskellCompositeElementType("HS_KIND_SIGNATURE");
    IElementType HS_LET_ABSTRACTION = new HaskellCompositeElementType("HS_LET_ABSTRACTION");
    IElementType HS_LIST_TYPE = new HaskellCompositeElementType("HS_LIST_TYPE");
    IElementType HS_MODID = HaskellElementTypeFactory.factory("HS_MODID");
    IElementType HS_MODULE_BODY = new HaskellCompositeElementType("HS_MODULE_BODY");
    IElementType HS_MODULE_DECLARATION = new HaskellCompositeElementType("HS_MODULE_DECLARATION");
    IElementType HS_NEWCONSTR = new HaskellCompositeElementType("HS_NEWCONSTR");
    IElementType HS_NEWCONSTR_FIELDDECL = new HaskellCompositeElementType("HS_NEWCONSTR_FIELDDECL");
    IElementType HS_NEWTYPE_DECLARATION = new HaskellCompositeElementType("HS_NEWTYPE_DECLARATION");
    IElementType HS_PAREN_EXPRESSION = new HaskellCompositeElementType("HS_PAREN_EXPRESSION");
    IElementType HS_PRAGMA = new HaskellCompositeElementType("HS_PRAGMA");
    IElementType HS_QUALIFIER = new HaskellCompositeElementType("HS_QUALIFIER");
    IElementType HS_QUASI_QUOTE = new HaskellCompositeElementType("HS_QUASI_QUOTE");
    IElementType HS_Q_CON = new HaskellCompositeElementType("HS_Q_CON");
    IElementType HS_Q_CON_QUALIFIER = new HaskellCompositeElementType("HS_Q_CON_QUALIFIER");
    IElementType HS_Q_CON_QUALIFIER_1 = new HaskellCompositeElementType("HS_Q_CON_QUALIFIER_1");
    IElementType HS_Q_CON_QUALIFIER_2 = new HaskellCompositeElementType("HS_Q_CON_QUALIFIER_2");
    IElementType HS_Q_CON_QUALIFIER_3 = new HaskellCompositeElementType("HS_Q_CON_QUALIFIER_3");
    IElementType HS_Q_CON_QUALIFIER_4 = new HaskellCompositeElementType("HS_Q_CON_QUALIFIER_4");
    IElementType HS_Q_NAME = new HaskellCompositeElementType("HS_Q_NAME");
    IElementType HS_Q_NAMES = new HaskellCompositeElementType("HS_Q_NAMES");
    IElementType HS_Q_VAR_CON = new HaskellCompositeElementType("HS_Q_VAR_CON");
    IElementType HS_SCONTEXT = new HaskellCompositeElementType("HS_SCONTEXT");
    IElementType HS_SHEBANG_LINE = new HaskellCompositeElementType("HS_SHEBANG_LINE");
    IElementType HS_SIMPLECLASS = new HaskellCompositeElementType("HS_SIMPLECLASS");
    IElementType HS_SIMPLETYPE = new HaskellCompositeElementType("HS_SIMPLETYPE");
    IElementType HS_TEXT_LITERAL = new HaskellCompositeElementType("HS_TEXT_LITERAL");
    IElementType HS_TOP_DECLARATION = new HaskellCompositeElementType("HS_TOP_DECLARATION");
    IElementType HS_TTYPE = new HaskellCompositeElementType("HS_TTYPE");
    IElementType HS_TYPE_DECLARATION = new HaskellCompositeElementType("HS_TYPE_DECLARATION");
    IElementType HS_TYPE_EQUALITY = new HaskellCompositeElementType("HS_TYPE_EQUALITY");
    IElementType HS_TYPE_FAMILY_DECLARATION = new HaskellCompositeElementType("HS_TYPE_FAMILY_DECLARATION");
    IElementType HS_TYPE_FAMILY_TYPE = new HaskellCompositeElementType("HS_TYPE_FAMILY_TYPE");
    IElementType HS_TYPE_INSTANCE_DECLARATION = new HaskellCompositeElementType("HS_TYPE_INSTANCE_DECLARATION");
    IElementType HS_TYPE_SIGNATURE = new HaskellCompositeElementType("HS_TYPE_SIGNATURE");
    IElementType HS_VARID = HaskellElementTypeFactory.factory("HS_VARID");
    IElementType HS_VARSYM = HaskellElementTypeFactory.factory("HS_VARSYM");
    IElementType HS_VAR_CON = new HaskellCompositeElementType("HS_VAR_CON");
    IElementType HS_WHERE_CLAUSE = new HaskellCompositeElementType("HS_WHERE_CLAUSE");

    IElementType HS_AT = new HaskellTokenType("AT");
    IElementType HS_BACKQUOTE = new HaskellTokenType("BACKQUOTE");
    IElementType HS_BACKSLASH = new HaskellTokenType("BACKSLASH");
    IElementType HS_CASE = new HaskellTokenType("CASE");
    IElementType HS_CHARACTER_LITERAL = new HaskellTokenType("CHARACTER_LITERAL");
    IElementType HS_CLASS = new HaskellTokenType("CLASS");
    IElementType HS_COLON_COLON = new HaskellTokenType("COLON_COLON");
    IElementType HS_COMMA = new HaskellTokenType("COMMA");
    IElementType HS_COMMENT = new HaskellTokenType("COMMENT");
    IElementType HS_CONSYM_ID = new HaskellTokenType("CONSYM_ID");
    IElementType HS_CON_ID = new HaskellTokenType("CON_ID");
    IElementType HS_DASH = new HaskellTokenType("DASH");
    IElementType HS_DATA = new HaskellTokenType("DATA");
    IElementType HS_DECIMAL = new HaskellTokenType("DECIMAL");
    IElementType HS_DEFAULT = new HaskellTokenType("DEFAULT");
    IElementType HS_DERIVING = new HaskellTokenType("DERIVING");
    IElementType HS_DIRECTIVE = new HaskellTokenType("DIRECTIVE");
    IElementType HS_DO = new HaskellTokenType("DO");
    IElementType HS_DOT = new HaskellTokenType("DOT");
    IElementType HS_DOUBLE_QUOTES = new HaskellTokenType("DOUBLE_QUOTES");
    IElementType HS_DOUBLE_RIGHT_ARROW = new HaskellTokenType("DOUBLE_RIGHT_ARROW");
    IElementType HS_ELSE = new HaskellTokenType("ELSE");
    IElementType HS_EQUAL = new HaskellTokenType("EQUAL");
    IElementType HS_FLOAT = new HaskellTokenType("FLOAT");
    IElementType HS_FORALL = new HaskellTokenType("FORALL");
    IElementType HS_FOREIGN_EXPORT = new HaskellTokenType("FOREIGN_EXPORT");
    IElementType HS_FOREIGN_IMPORT = new HaskellTokenType("FOREIGN_IMPORT");
    IElementType HS_HADDOCK = new HaskellTokenType("HADDOCK");
    IElementType HS_HASH = new HaskellTokenType("HASH");
    IElementType HS_HEXADECIMAL = new HaskellTokenType("HEXADECIMAL");
    IElementType HS_IF = new HaskellTokenType("IF");
    IElementType HS_IMPORT = new HaskellTokenType("IMPORT");
    IElementType HS_IN = new HaskellTokenType("IN");
    IElementType HS_INCLUDE_DIRECTIVE = new HaskellTokenType("INCLUDE_DIRECTIVE");
    IElementType HS_INFIX = new HaskellTokenType("INFIX");
    IElementType HS_INFIXL = new HaskellTokenType("INFIXL");
    IElementType HS_INFIXR = new HaskellTokenType("INFIXR");
    IElementType HS_INSTANCE = new HaskellTokenType("INSTANCE");
    IElementType HS_LEFT_ARROW = new HaskellTokenType("LEFT_ARROW");
    IElementType HS_LEFT_BRACE = new HaskellTokenType("LEFT_BRACE");
    IElementType HS_LEFT_BRACKET = new HaskellTokenType("LEFT_BRACKET");
    IElementType HS_LEFT_PAREN = new HaskellTokenType("LEFT_PAREN");
    IElementType HS_LET = new HaskellTokenType("LET");
    IElementType HS_LIST_COMPREHENSION = new HaskellTokenType("LIST_COMPREHENSION");
    IElementType HS_MODULE = new HaskellTokenType("MODULE");
    IElementType HS_NCOMMENT = new HaskellTokenType("NCOMMENT");
    IElementType HS_NEWLINE = new HaskellTokenType("NEWLINE");
    IElementType HS_NEWTYPE = new HaskellTokenType("NEWTYPE");
    IElementType HS_NHADDOCK = new HaskellTokenType("NHADDOCK");
    IElementType HS_NOT_TERMINATED_COMMENT = new HaskellTokenType("NOT_TERMINATED_COMMENT");
    IElementType HS_NOT_TERMINATED_QQ_EXPRESSION = new HaskellTokenType("NOT_TERMINATED_QQ_EXPRESSION");
    IElementType HS_OCTAL = new HaskellTokenType("OCTAL");
    IElementType HS_OF = new HaskellTokenType("OF");
    IElementType HS_ONE_PRAGMA = new HaskellTokenType("ONE_PRAGMA");
    IElementType HS_PRAGMA_END = new HaskellTokenType("PRAGMA_END");
    IElementType HS_PRAGMA_SEP = new HaskellTokenType("PRAGMA_SEP");
    IElementType HS_PRAGMA_START = new HaskellTokenType("PRAGMA_START");
    IElementType HS_QUASIQUOTE = new HaskellTokenType("QUASIQUOTE");
    IElementType HS_QUOTE = new HaskellTokenType("QUOTE");
    IElementType HS_RIGHT_ARROW = new HaskellTokenType("RIGHT_ARROW");
    IElementType HS_RIGHT_BRACE = new HaskellTokenType("RIGHT_BRACE");
    IElementType HS_RIGHT_BRACKET = new HaskellTokenType("RIGHT_BRACKET");
    IElementType HS_RIGHT_PAREN = new HaskellTokenType("RIGHT_PAREN");
    IElementType HS_SEMI = new HaskellTokenType("semi");
    IElementType HS_SEMICOLON = new HaskellTokenType("SEMICOLON");
    IElementType HS_STRING_LITERAL = new HaskellTokenType("STRING_LITERAL");
    IElementType HS_THEN = new HaskellTokenType("THEN");
    IElementType HS_TILDE = new HaskellTokenType("TILDE");
    IElementType HS_TYPE = new HaskellTokenType("TYPE");
    IElementType HS_TYPE_FAMILY = new HaskellTokenType("TYPE_FAMILY");
    IElementType HS_TYPE_INSTANCE = new HaskellTokenType("TYPE_INSTANCE");
    IElementType HS_UNDERSCORE = new HaskellTokenType("UNDERSCORE");
    IElementType HS_VARSYM_ID = new HaskellTokenType("VARSYM_ID");
    IElementType HS_VAR_ID = new HaskellTokenType("VAR_ID");
    IElementType HS_VERTICAL_BAR = new HaskellTokenType("VERTICAL_BAR");
    IElementType HS_WHERE = new HaskellTokenType("WHERE");

    class Factory {
        public static PsiElement createElement(ASTNode node) {
            IElementType type = node.getElementType();
            if (type == HS_APPLICATION_EXPRESSION) {
                return new HaskellApplicationExpressionImpl(node);
            } else if (type == HS_ATOM_EXPRESSION) {
                return new HaskellAtomExpressionImpl(node);
            } else if (type == HS_BRACKET_EXPRESSION) {
                return new HaskellBracketExpressionImpl(node);
            } else if (type == HS_CASE_CLAUSE) {
                return new HaskellCaseClauseImpl(node);
            } else if (type == HS_CASE_OF_EXPRESSION) {
                return new HaskellCaseOfExpressionImpl(node);
            } else if (type == HS_CCONTEXT) {
                return new HaskellCcontextImpl(node);
            } else if (type == HS_CDECL) {
                return new HaskellCdeclImpl(node);
            } else if (type == HS_CDECLS) {
                return new HaskellCdeclsImpl(node);
            } else if (type == HS_CDECL_DATA_DECLARATION) {
                return new HaskellCdeclDataDeclarationImpl(node);
            } else if (type == HS_CIDECL) {
                return new HaskellCideclImpl(node);
            } else if (type == HS_CIDECLS) {
                return new HaskellCideclsImpl(node);
            } else if (type == HS_CLASS_DECLARATION) {
                return new HaskellClassDeclarationImpl(node);
            } else if (type == HS_CLAZZ) {
                return new HaskellClazzImpl(node);
            } else if (type == HS_COMMENTS) {
                return new HaskellCommentsImpl(node);
            } else if (type == HS_CONID) {
                return new HaskellConidImpl(node);
            } else if (type == HS_CONSTR) {
                return new HaskellConstrImpl(node);
            } else if (type == HS_CONSTR_1) {
                return new HaskellConstr1Impl(node);
            } else if (type == HS_CONSTR_2) {
                return new HaskellConstr2Impl(node);
            } else if (type == HS_CONSTR_3) {
                return new HaskellConstr3Impl(node);
            } else if (type == HS_CONSYM) {
                return new HaskellConsymImpl(node);
            } else if (type == HS_DATA_DECLARATION) {
                return new HaskellDataDeclarationImpl(node);
            } else if (type == HS_DATA_DECLARATION_DERIVING) {
                return new HaskellDataDeclarationDerivingImpl(node);
            } else if (type == HS_DEFAULT_DECLARATION) {
                return new HaskellDefaultDeclarationImpl(node);
            } else if (type == HS_DERIVING_DECLARATION) {
                return new HaskellDerivingDeclarationImpl(node);
            } else if (type == HS_DERIVING_VIA) {
                return new HaskellDerivingViaImpl(node);
            } else if (type == HS_DOT_DOT) {
                return new HaskellDotDotImpl(node);
            } else if (type == HS_DO_NOTATION_EXPRESSION) {
                return new HaskellDoNotationExpressionImpl(node);
            } else if (type == HS_EXPORT) {
                return new HaskellExportImpl(node);
            } else if (type == HS_EXPORTS) {
                return new HaskellExportsImpl(node);
            } else if (type == HS_EXPRESSION) {
                return new HaskellExpressionImpl(node);
            } else if (type == HS_FIELDDECL) {
                return new HaskellFielddeclImpl(node);
            } else if (type == HS_FILE_HEADER) {
                return new HaskellFileHeaderImpl(node);
            } else if (type == HS_FIXITY_DECLARATION) {
                return new HaskellFixityDeclarationImpl(node);
            } else if (type == HS_FOREIGN_DECLARATION) {
                return new HaskellForeignDeclarationImpl(node);
            } else if (type == HS_GENERAL_PRAGMA_CONTENT) {
                return new HaskellGeneralPragmaContentImpl(node);
            } else if (type == HS_GTYCON) {
                return new HaskellGtyconImpl(node);
            } else if (type == HS_IF_EXPRESSION) {
                return new HaskellIfExpressionImpl(node);
            } else if (type == HS_IMPLEMENTATION_DECLARATION) {
                return new HaskellImplementationDeclarationImpl(node);
            } else if (type == HS_IMPORT_DECLARATION) {
                return new HaskellImportDeclarationImpl(node);
            } else if (type == HS_IMPORT_DECLARATIONS) {
                return new HaskellImportDeclarationsImpl(node);
            } else if (type == HS_IMPORT_EMPTY_SPEC) {
                return new HaskellImportEmptySpecImpl(node);
            } else if (type == HS_IMPORT_HIDING) {
                return new HaskellImportHidingImpl(node);
            } else if (type == HS_IMPORT_HIDING_SPEC) {
                return new HaskellImportHidingSpecImpl(node);
            } else if (type == HS_IMPORT_ID) {
                return new HaskellImportIdImpl(node);
            } else if (type == HS_IMPORT_IDS_SPEC) {
                return new HaskellImportIdsSpecImpl(node);
            } else if (type == HS_IMPORT_PACKAGE_NAME) {
                return new HaskellImportPackageNameImpl(node);
            } else if (type == HS_IMPORT_QUALIFIED) {
                return new HaskellImportQualifiedImpl(node);
            } else if (type == HS_IMPORT_QUALIFIED_AS) {
                return new HaskellImportQualifiedAsImpl(node);
            } else if (type == HS_IMPORT_SPEC) {
                return new HaskellImportSpecImpl(node);
            } else if (type == HS_INST) {
                return new HaskellInstImpl(node);
            } else if (type == HS_INSTANCE_DECLARATION) {
                return new HaskellInstanceDeclarationImpl(node);
            } else if (type == HS_INSTVAR) {
                return new HaskellInstvarImpl(node);
            } else if (type == HS_KIND_SIGNATURE) {
                return new HaskellKindSignatureImpl(node);
            } else if (type == HS_LET_ABSTRACTION) {
                return new HaskellLetAbstractionImpl(node);
            } else if (type == HS_LIST_TYPE) {
                return new HaskellListTypeImpl(node);
            } else if (type == HS_MODID) {
                return new HaskellModidImpl(node);
            } else if (type == HS_MODULE_BODY) {
                return new HaskellModuleBodyImpl(node);
            } else if (type == HS_MODULE_DECLARATION) {
                return new HaskellModuleDeclarationImpl(node);
            } else if (type == HS_NEWCONSTR) {
                return new HaskellNewconstrImpl(node);
            } else if (type == HS_NEWCONSTR_FIELDDECL) {
                return new HaskellNewconstrFielddeclImpl(node);
            } else if (type == HS_NEWTYPE_DECLARATION) {
                return new HaskellNewtypeDeclarationImpl(node);
            } else if (type == HS_PAREN_EXPRESSION) {
                return new HaskellParenExpressionImpl(node);
            } else if (type == HS_PRAGMA) {
                return new HaskellPragmaImpl(node);
            } else if (type == HS_QUALIFIER) {
                return new HaskellQualifierImpl(node);
            } else if (type == HS_QUASI_QUOTE) {
                return new HaskellQuasiQuoteImpl(node);
            } else if (type == HS_Q_CON) {
                return new HaskellQConImpl(node);
            } else if (type == HS_Q_CON_QUALIFIER) {
                return new HaskellQConQualifierImpl(node);
            } else if (type == HS_Q_CON_QUALIFIER_1) {
                return new HaskellQConQualifier1Impl(node);
            } else if (type == HS_Q_CON_QUALIFIER_2) {
                return new HaskellQConQualifier2Impl(node);
            } else if (type == HS_Q_CON_QUALIFIER_3) {
                return new HaskellQConQualifier3Impl(node);
            } else if (type == HS_Q_CON_QUALIFIER_4) {
                return new HaskellQConQualifier4Impl(node);
            } else if (type == HS_Q_NAME) {
                return new HaskellQNameImpl(node);
            } else if (type == HS_Q_NAMES) {
                return new HaskellQNamesImpl(node);
            } else if (type == HS_Q_VAR_CON) {
                return new HaskellQVarConImpl(node);
            } else if (type == HS_SCONTEXT) {
                return new HaskellScontextImpl(node);
            } else if (type == HS_SHEBANG_LINE) {
                return new HaskellShebangLineImpl(node);
            } else if (type == HS_SIMPLECLASS) {
                return new HaskellSimpleclassImpl(node);
            } else if (type == HS_SIMPLETYPE) {
                return new HaskellSimpletypeImpl(node);
            } else if (type == HS_TEXT_LITERAL) {
                return new HaskellTextLiteralImpl(node);
            } else if (type == HS_TOP_DECLARATION) {
                return new HaskellTopDeclarationImpl(node);
            } else if (type == HS_TTYPE) {
                return new HaskellTtypeImpl(node);
            } else if (type == HS_TYPE_DECLARATION) {
                return new HaskellTypeDeclarationImpl(node);
            } else if (type == HS_TYPE_EQUALITY) {
                return new HaskellTypeEqualityImpl(node);
            } else if (type == HS_TYPE_FAMILY_DECLARATION) {
                return new HaskellTypeFamilyDeclarationImpl(node);
            } else if (type == HS_TYPE_FAMILY_TYPE) {
                return new HaskellTypeFamilyTypeImpl(node);
            } else if (type == HS_TYPE_INSTANCE_DECLARATION) {
                return new HaskellTypeInstanceDeclarationImpl(node);
            } else if (type == HS_TYPE_SIGNATURE) {
                return new HaskellTypeSignatureImpl(node);
            } else if (type == HS_VARID) {
                return new HaskellVaridImpl(node);
            } else if (type == HS_VARSYM) {
                return new HaskellVarsymImpl(node);
            } else if (type == HS_VAR_CON) {
                return new HaskellVarConImpl(node);
            } else if (type == HS_WHERE_CLAUSE) {
                return new HaskellWhereClauseImpl(node);
            }
            throw new AssertionError("Unknown element type: " + type);
        }
    }
}
