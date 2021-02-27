// This is a generated file. Not intended for manual editing.
package intellij.haskell.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static intellij.haskell.psi.HaskellParserUtil.*;
import static intellij.haskell.psi.HaskellTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class HaskellParser implements PsiParser, LightPsiParser {

    public ASTNode parse(IElementType t, PsiBuilder b) {
        parseLight(t, b);
        return b.getTreeBuilt();
    }

    public void parseLight(IElementType t, PsiBuilder b) {
        boolean r;
        b = adapt_builder_(t, b, this, EXTENDS_SETS_);
        Marker m = enter_section_(b, 0, _COLLAPSE_, null);
        r = parse_root_(t, b);
        exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
    }

    protected boolean parse_root_(IElementType t, PsiBuilder b) {
        return parse_root_(t, b, 0);
    }

    static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
        return program(b, l + 1);
    }

    public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[]{
            create_token_set_(HS_APPLICATION_EXPRESSION, HS_ATOM_EXPRESSION, HS_BRACKET_EXPRESSION, HS_CASE_OF_EXPRESSION,
                    HS_DO_NOTATION_EXPRESSION, HS_EXPRESSION, HS_IF_EXPRESSION, HS_LET_ABSTRACTION,
                    HS_PAREN_EXPRESSION),
            create_token_set_(HS_CDECL_DATA_DECLARATION, HS_CLASS_DECLARATION, HS_DATA_DECLARATION, HS_DEFAULT_DECLARATION,
                    HS_DERIVING_DECLARATION, HS_FIXITY_DECLARATION, HS_FOREIGN_DECLARATION, HS_IMPLEMENTATION_DECLARATION,
                    HS_IMPORT_DECLARATION, HS_INSTANCE_DECLARATION, HS_MODULE_DECLARATION, HS_NEWTYPE_DECLARATION,
                    HS_TOP_DECLARATION, HS_TYPE_DECLARATION, HS_TYPE_FAMILY_DECLARATION, HS_TYPE_INSTANCE_DECLARATION),
    };

    /* ********************************************************** */
    // expression atom+
    public static boolean application_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "application_expression")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, HS_APPLICATION_EXPRESSION, "<application expression>");
        r = expression(b, l + 1);
        r = r && application_expression_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // atom+
    private static boolean application_expression_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "application_expression_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atom(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atom(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "application_expression_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // atom_expression | bracket_expression | paren_expression
    static boolean atom(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atom")) return false;
        boolean r;
        r = atom_expression(b, l + 1);
        if (!r) r = bracket_expression(b, l + 1);
        if (!r) r = paren_expression(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // general_id
    public static boolean atom_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atom_expression")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_ATOM_EXPRESSION, "<atom expression>");
        r = general_id(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // (("forall" | FORALL) onl (((q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN)+ | onl type_signature | (onls q_name)+) (onl DOUBLE_RIGHT_ARROW onl ttype)? |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype1 RIGHT_PAREN |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype1 RIGHT_BRACE |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype2 RIGHT_PAREN |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype2 RIGHT_BRACE |
    //                                   QUOTE? LEFT_BRACKET oonls ttype (oonls COMMA oonls ttype)* oonls RIGHT_BRACKET |
    //                                   QUOTE? ((deriving_via | q_name) (oonls COLON_COLON oonls ttype)?)+ | QUOTE? LEFT_PAREN RIGHT_PAREN | QUOTE? LEFT_BRACKET RIGHT_BRACKET | LEFT_PAREN COMMA+ RIGHT_PAREN  | literal | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN) !COLON_COLON
    static boolean atype(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0(b, l + 1);
        r = r && atype_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // ("forall" | FORALL) onl (((q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN)+ | onl type_signature | (onls q_name)+) (onl DOUBLE_RIGHT_ARROW onl ttype)? |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype1 RIGHT_PAREN |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype1 RIGHT_BRACE |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype2 RIGHT_PAREN |
    //                                   (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype2 RIGHT_BRACE |
    //                                   QUOTE? LEFT_BRACKET oonls ttype (oonls COMMA oonls ttype)* oonls RIGHT_BRACKET |
    //                                   QUOTE? ((deriving_via | q_name) (oonls COLON_COLON oonls ttype)?)+ | QUOTE? LEFT_PAREN RIGHT_PAREN | QUOTE? LEFT_BRACKET RIGHT_BRACKET | LEFT_PAREN COMMA+ RIGHT_PAREN  | literal | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN
    private static boolean atype_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0(b, l + 1);
        if (!r) r = atype_0_1(b, l + 1);
        if (!r) r = atype_0_2(b, l + 1);
        if (!r) r = atype_0_3(b, l + 1);
        if (!r) r = atype_0_4(b, l + 1);
        if (!r) r = atype_0_5(b, l + 1);
        if (!r) r = atype_0_6(b, l + 1);
        if (!r) r = atype_0_7(b, l + 1);
        if (!r) r = atype_0_8(b, l + 1);
        if (!r) r = atype_0_9(b, l + 1);
        if (!r) r = literal(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_ARROW, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // ("forall" | FORALL) onl (((q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN)+ | onl type_signature | (onls q_name)+) (onl DOUBLE_RIGHT_ARROW onl ttype)?
    private static boolean atype_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && atype_0_0_2(b, l + 1);
        r = r && atype_0_0_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // "forall" | FORALL
    private static boolean atype_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_0")) return false;
        boolean r;
        r = consumeToken(b, "forall");
        if (!r) r = consumeToken(b, HS_FORALL);
        return r;
    }

    // ((q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN)+ | onl type_signature | (onls q_name)+
    private static boolean atype_0_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_2_0(b, l + 1);
        if (!r) r = atype_0_0_2_1(b, l + 1);
        if (!r) r = atype_0_0_2_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // ((q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN)+
    private static boolean atype_0_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_2_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atype_0_0_2_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_0_2_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name onls)+ LEFT_PAREN onl (type_signature | ttype) onl (COMMA onl (type_signature | ttype))* onl RIGHT_PAREN
    private static boolean atype_0_0_2_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_2_0_0_0(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onl(b, l + 1);
        r = r && atype_0_0_2_0_0_3(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && atype_0_0_2_0_0_5(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name onls)+
    private static boolean atype_0_0_2_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_2_0_0_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atype_0_0_2_0_0_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_0_2_0_0_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name onls
    private static boolean atype_0_0_2_0_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && onls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // type_signature | ttype
    private static boolean atype_0_0_2_0_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_3")) return false;
        boolean r;
        r = type_signature(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        return r;
    }

    // (COMMA onl (type_signature | ttype))*
    private static boolean atype_0_0_2_0_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_5")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_0_2_0_0_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_0_2_0_0_5", c)) break;
        }
        return true;
    }

    // COMMA onl (type_signature | ttype)
    private static boolean atype_0_0_2_0_0_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COMMA);
        r = r && onl(b, l + 1);
        r = r && atype_0_0_2_0_0_5_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // type_signature | ttype
    private static boolean atype_0_0_2_0_0_5_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_0_0_5_0_2")) return false;
        boolean r;
        r = type_signature(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        return r;
    }

    // onl type_signature
    private static boolean atype_0_0_2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && type_signature(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls q_name)+
    private static boolean atype_0_0_2_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_0_2_2_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atype_0_0_2_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_0_2_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // onls q_name
    private static boolean atype_0_0_2_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_2_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl DOUBLE_RIGHT_ARROW onl ttype)?
    private static boolean atype_0_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_3")) return false;
        atype_0_0_3_0(b, l + 1);
        return true;
    }

    // onl DOUBLE_RIGHT_ARROW onl ttype
    private static boolean atype_0_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype1 RIGHT_PAREN
    private static boolean atype_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_1_0(b, l + 1);
        r = r && atype_0_1_1(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && ttype1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name | UNDERSCORE)*
    private static boolean atype_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_1_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_1_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_1_0", c)) break;
        }
        return true;
    }

    // q_name | UNDERSCORE
    private static boolean atype_0_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_1_0_0")) return false;
        boolean r;
        r = q_name(b, l + 1);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_1_1")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype1 RIGHT_BRACE
    private static boolean atype_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_2_0(b, l + 1);
        r = r && atype_0_2_1(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_BRACE);
        r = r && ttype1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name | UNDERSCORE)*
    private static boolean atype_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_2_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_2_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_2_0", c)) break;
        }
        return true;
    }

    // q_name | UNDERSCORE
    private static boolean atype_0_2_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_2_0_0")) return false;
        boolean r;
        r = q_name(b, l + 1);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_2_1")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // (q_name | UNDERSCORE)* QUOTE? LEFT_PAREN ttype2 RIGHT_PAREN
    private static boolean atype_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_3_0(b, l + 1);
        r = r && atype_0_3_1(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && ttype2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name | UNDERSCORE)*
    private static boolean atype_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_3_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_3_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_3_0", c)) break;
        }
        return true;
    }

    // q_name | UNDERSCORE
    private static boolean atype_0_3_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_3_0_0")) return false;
        boolean r;
        r = q_name(b, l + 1);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_3_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_3_1")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // (q_name | UNDERSCORE)* QUOTE? LEFT_BRACE ttype2 RIGHT_BRACE
    private static boolean atype_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_4_0(b, l + 1);
        r = r && atype_0_4_1(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_BRACE);
        r = r && ttype2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name | UNDERSCORE)*
    private static boolean atype_0_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_4_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_4_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_4_0", c)) break;
        }
        return true;
    }

    // q_name | UNDERSCORE
    private static boolean atype_0_4_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_4_0_0")) return false;
        boolean r;
        r = q_name(b, l + 1);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_4_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_4_1")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // QUOTE? LEFT_BRACKET oonls ttype (oonls COMMA oonls ttype)* oonls RIGHT_BRACKET
    private static boolean atype_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_5")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_5_0(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_BRACKET);
        r = r && oonls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && atype_0_5_4(b, l + 1);
        r = r && oonls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_5_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // (oonls COMMA oonls ttype)*
    private static boolean atype_0_5_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_5_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!atype_0_5_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_5_4", c)) break;
        }
        return true;
    }

    // oonls COMMA oonls ttype
    private static boolean atype_0_5_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_5_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && oonls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE? ((deriving_via | q_name) (oonls COLON_COLON oonls ttype)?)+
    private static boolean atype_0_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_6_0(b, l + 1);
        r = r && atype_0_6_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // ((deriving_via | q_name) (oonls COLON_COLON oonls ttype)?)+
    private static boolean atype_0_6_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_6_1_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atype_0_6_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_6_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (deriving_via | q_name) (oonls COLON_COLON oonls ttype)?
    private static boolean atype_0_6_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_6_1_0_0(b, l + 1);
        r = r && atype_0_6_1_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // deriving_via | q_name
    private static boolean atype_0_6_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_1_0_0")) return false;
        boolean r;
        r = deriving_via(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        return r;
    }

    // (oonls COLON_COLON oonls ttype)?
    private static boolean atype_0_6_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_1_0_1")) return false;
        atype_0_6_1_0_1_0(b, l + 1);
        return true;
    }

    // oonls COLON_COLON oonls ttype
    private static boolean atype_0_6_1_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_6_1_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && oonls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE? LEFT_PAREN RIGHT_PAREN
    private static boolean atype_0_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_7")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_7_0(b, l + 1);
        r = r && consumeTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_7_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // QUOTE? LEFT_BRACKET RIGHT_BRACKET
    private static boolean atype_0_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_8")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype_0_8_0(b, l + 1);
        r = r && consumeTokens(b, 0, HS_LEFT_BRACKET, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean atype_0_8_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_8_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // LEFT_PAREN COMMA+ RIGHT_PAREN
    private static boolean atype_0_9(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_9")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && atype_0_9_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // COMMA+
    private static boolean atype_0_9_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_0_9_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COMMA);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_COMMA)) break;
            if (!empty_element_parsed_guard_(b, "atype_0_9_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // !COLON_COLON
    private static boolean atype_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "atype_1")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NOT_);
        r = !consumeToken(b, HS_COLON_COLON);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // import_declarations? onl top_declarations onl
    static boolean body(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "body")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = body_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && top_declarations(b, l + 1);
        r = r && onl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // import_declarations?
    private static boolean body_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "body_0")) return false;
        import_declarations(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // LEFT_BRACKET expression RIGHT_BRACKET
    public static boolean bracket_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "bracket_expression")) return false;
        if (!nextTokenIs(b, HS_LEFT_BRACKET)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_BRACKET);
        r = r && expression(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, HS_BRACKET_EXPRESSION, r);
        return r;
    }

    /* ********************************************************** */
    // (atype | TILDE | UNDERSCORE)+
    static boolean btype(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "btype")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = btype_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!btype_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "btype", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // atype | TILDE | UNDERSCORE
    private static boolean btype_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "btype_0")) return false;
        boolean r;
        r = atype(b, l + 1);
        if (!r) r = consumeToken(b, HS_TILDE);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    /* ********************************************************** */
    // expression RIGHT_ARROW expression
    public static boolean case_clause(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_clause")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_CASE_CLAUSE, "<case clause>");
        r = expression(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_ARROW);
        p = r; // pin = 2
        r = r && expression(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // case_clause SEMICOLON
    static boolean case_definition(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_definition")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = case_clause(b, l + 1);
        r = r && consumeToken(b, HS_SEMICOLON);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // CASE expression OF LEFT_BRACE case_definition* case_clause? RIGHT_BRACE?
    public static boolean case_of_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_of_expression")) return false;
        if (!nextTokenIs(b, HS_CASE)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_CASE_OF_EXPRESSION, null);
        r = consumeToken(b, HS_CASE);
        p = r; // pin = 1
        r = r && report_error_(b, expression(b, l + 1));
        r = p && report_error_(b, consumeTokens(b, -1, HS_OF, HS_LEFT_BRACE)) && r;
        r = p && report_error_(b, case_of_expression_4(b, l + 1)) && r;
        r = p && report_error_(b, case_of_expression_5(b, l + 1)) && r;
        r = p && case_of_expression_6(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // case_definition*
    private static boolean case_of_expression_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_of_expression_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!case_definition(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "case_of_expression_4", c)) break;
        }
        return true;
    }

    // case_clause?
    private static boolean case_of_expression_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_of_expression_5")) return false;
        case_clause(b, l + 1);
        return true;
    }

    // RIGHT_BRACE?
    private static boolean case_of_expression_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "case_of_expression_6")) return false;
        consumeToken(b, HS_RIGHT_BRACE);
        return true;
    }

    /* ********************************************************** */
    // LEFT_PAREN onls clazz (onls COMMA onls clazz)* onls RIGHT_PAREN |
    //                                   clazz
    public static boolean ccontext(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ccontext")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CCONTEXT, "<ccontext>");
        r = ccontext_0(b, l + 1);
        if (!r) r = clazz(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // LEFT_PAREN onls clazz (onls COMMA onls clazz)* onls RIGHT_PAREN
    private static boolean ccontext_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ccontext_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && clazz(b, l + 1);
        r = r && ccontext_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls clazz)*
    private static boolean ccontext_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ccontext_0_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!ccontext_0_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "ccontext_0_3", c)) break;
        }
        return true;
    }

    // onls COMMA onls clazz
    private static boolean ccontext_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ccontext_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && clazz(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // type_signature | cdecl_data_declaration | cidecl
    public static boolean cdecl(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CDECL, "<cdecl>");
        r = type_signature(b, l + 1);
        if (!r) r = cdecl_data_declaration(b, l + 1);
        if (!r) r = cidecl(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // DATA (onls pragma)? (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls simpletype (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* (onls COLON_COLON ttype)? q_name*
    public static boolean cdecl_data_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration")) return false;
        if (!nextTokenIs(b, HS_DATA)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DATA);
        r = r && cdecl_data_declaration_1(b, l + 1);
        r = r && cdecl_data_declaration_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && cdecl_data_declaration_5(b, l + 1);
        r = r && cdecl_data_declaration_6(b, l + 1);
        r = r && cdecl_data_declaration_7(b, l + 1);
        exit_section_(b, m, HS_CDECL_DATA_DECLARATION, r);
        return r;
    }

    // (onls pragma)?
    private static boolean cdecl_data_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_1")) return false;
        cdecl_data_declaration_1_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean cdecl_data_declaration_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean cdecl_data_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!cdecl_data_declaration_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "cdecl_data_declaration_2", c)) break;
        }
        return true;
    }

    // onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean cdecl_data_declaration_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean cdecl_data_declaration_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_5")) return false;
        while (true) {
            int c = current_position_(b);
            if (!cdecl_data_declaration_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "cdecl_data_declaration_5", c)) break;
        }
        return true;
    }

    // onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean cdecl_data_declaration_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COLON_COLON ttype)?
    private static boolean cdecl_data_declaration_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_6")) return false;
        cdecl_data_declaration_6_0(b, l + 1);
        return true;
    }

    // onls COLON_COLON ttype
    private static boolean cdecl_data_declaration_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean cdecl_data_declaration_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecl_data_declaration_7")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "cdecl_data_declaration_7", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // cdecl (nls cdecl)*
    public static boolean cdecls(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecls")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CDECLS, "<cdecls>");
        r = cdecl(b, l + 1);
        r = r && cdecls_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (nls cdecl)*
    private static boolean cdecls_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecls_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!cdecls_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "cdecls_1", c)) break;
        }
        return true;
    }

    // nls cdecl
    private static boolean cdecls_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cdecls_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls(b, l + 1);
        r = r && cdecl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // pragma | instance_declaration | default_declaration |
    //                                   newtype_declaration | data_declaration | type_declaration | type_family_declaration
    //                                   | line_expression | implementation_declaration
    public static boolean cidecl(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cidecl")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CIDECL, "<cidecl>");
        r = pragma(b, l + 1);
        if (!r) r = instance_declaration(b, l + 1);
        if (!r) r = default_declaration(b, l + 1);
        if (!r) r = newtype_declaration(b, l + 1);
        if (!r) r = data_declaration(b, l + 1);
        if (!r) r = type_declaration(b, l + 1);
        if (!r) r = type_family_declaration(b, l + 1);
        if (!r) r = line_expression(b, l + 1);
        if (!r) r = implementation_declaration(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // cidecl (nls cidecl)*
    public static boolean cidecls(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cidecls")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CIDECLS, "<cidecls>");
        r = cidecl(b, l + 1);
        r = r && cidecls_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (nls cidecl)*
    private static boolean cidecls_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cidecls_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!cidecls_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "cidecls_1", c)) break;
        }
        return true;
    }

    // nls cidecl
    private static boolean cidecls_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "cidecls_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls(b, l + 1);
        r = r && cidecl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // CLASS (onls scontext onls DOUBLE_RIGHT_ARROW)? onls (q_name+ | ttype) (onls q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
    //                                     (onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*)? (onls WHERE)? (onls cdecls)? |
    //                                   CLASS onls scontext onls DOUBLE_RIGHT_ARROW onls (q_name+ | ttype) (onls WHERE)? (onls cdecls)?
    public static boolean class_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration")) return false;
        if (!nextTokenIs(b, HS_CLASS)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = class_declaration_0(b, l + 1);
        if (!r) r = class_declaration_1(b, l + 1);
        exit_section_(b, m, HS_CLASS_DECLARATION, r);
        return r;
    }

    // CLASS (onls scontext onls DOUBLE_RIGHT_ARROW)? onls (q_name+ | ttype) (onls q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
    //                                     (onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*)? (onls WHERE)? (onls cdecls)?
    private static boolean class_declaration_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_CLASS);
        r = r && class_declaration_0_1(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && class_declaration_0_3(b, l + 1);
        r = r && class_declaration_0_4(b, l + 1);
        r = r && class_declaration_0_5(b, l + 1);
        r = r && class_declaration_0_6(b, l + 1);
        r = r && class_declaration_0_7(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls scontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean class_declaration_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_1")) return false;
        class_declaration_0_1_0(b, l + 1);
        return true;
    }

    // onls scontext onls DOUBLE_RIGHT_ARROW
    private static boolean class_declaration_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && scontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+ | ttype
    private static boolean class_declaration_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = class_declaration_0_3_0(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean class_declaration_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "class_declaration_0_3_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
    private static boolean class_declaration_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!class_declaration_0_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "class_declaration_0_4", c)) break;
        }
        return true;
    }

    // onls q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
    private static boolean class_declaration_0_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = class_declaration_0_4_0_0(b, l + 1);
        if (!r) r = class_declaration_0_4_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onls q_name
    private static boolean class_declaration_0_4_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
    private static boolean class_declaration_0_4_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && ttype(b, l + 1);
        r = r && class_declaration_0_4_0_1_2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls ttype)*
    private static boolean class_declaration_0_4_0_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4_0_1_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!class_declaration_0_4_0_1_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "class_declaration_0_4_0_1_2", c)) break;
        }
        return true;
    }

    // onls COMMA onls ttype
    private static boolean class_declaration_0_4_0_1_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_4_0_1_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*)?
    private static boolean class_declaration_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_5")) return false;
        class_declaration_0_5_0(b, l + 1);
        return true;
    }

    // onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*
    private static boolean class_declaration_0_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_VERTICAL_BAR);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && class_declaration_0_5_0_4(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls ttype)*
    private static boolean class_declaration_0_5_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_5_0_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!class_declaration_0_5_0_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "class_declaration_0_5_0_4", c)) break;
        }
        return true;
    }

    // onls COMMA onls ttype
    private static boolean class_declaration_0_5_0_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_5_0_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls WHERE)?
    private static boolean class_declaration_0_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_6")) return false;
        class_declaration_0_6_0(b, l + 1);
        return true;
    }

    // onls WHERE
    private static boolean class_declaration_0_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_WHERE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls cdecls)?
    private static boolean class_declaration_0_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_7")) return false;
        class_declaration_0_7_0(b, l + 1);
        return true;
    }

    // onls cdecls
    private static boolean class_declaration_0_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_0_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && cdecls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // CLASS onls scontext onls DOUBLE_RIGHT_ARROW onls (q_name+ | ttype) (onls WHERE)? (onls cdecls)?
    private static boolean class_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_CLASS);
        r = r && onls(b, l + 1);
        r = r && scontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        r = r && onls(b, l + 1);
        r = r && class_declaration_1_6(b, l + 1);
        r = r && class_declaration_1_7(b, l + 1);
        r = r && class_declaration_1_8(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+ | ttype
    private static boolean class_declaration_1_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = class_declaration_1_6_0(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean class_declaration_1_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "class_declaration_1_6_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls WHERE)?
    private static boolean class_declaration_1_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_7")) return false;
        class_declaration_1_7_0(b, l + 1);
        return true;
    }

    // onls WHERE
    private static boolean class_declaration_1_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_WHERE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls cdecls)?
    private static boolean class_declaration_1_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_8")) return false;
        class_declaration_1_8_0(b, l + 1);
        return true;
    }

    // onls cdecls
    private static boolean class_declaration_1_8_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "class_declaration_1_8_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && cdecls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_name COLON_COLON q_name | ttype |
    //                                   q_name+ |
    //                                   q_name LEFT_PAREN q_name atype+ RIGHT_PAREN |
    //                                   q_name LEFT_PAREN q_name+ RIGHT_PAREN q_name*
    public static boolean clazz(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CLAZZ, "<clazz>");
        r = clazz_0(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        if (!r) r = clazz_2(b, l + 1);
        if (!r) r = clazz_3(b, l + 1);
        if (!r) r = clazz_4(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_name COLON_COLON q_name
    private static boolean clazz_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean clazz_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "clazz_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name LEFT_PAREN q_name atype+ RIGHT_PAREN
    private static boolean clazz_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && q_name(b, l + 1);
        r = r && clazz_3_3(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // atype+
    private static boolean clazz_3_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_3_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = atype(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!atype(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "clazz_3_3", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name LEFT_PAREN q_name+ RIGHT_PAREN q_name*
    private static boolean clazz_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && clazz_4_2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        r = r && clazz_4_4(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean clazz_4_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_4_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "clazz_4_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean clazz_4_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "clazz_4_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "clazz_4_4", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // COMMENT | NCOMMENT | HADDOCK | NHADDOCK | NOT_TERMINATED_COMMENT
    public static boolean comments(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "comments")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_COMMENTS, "<comments>");
        r = consumeToken(b, HS_COMMENT);
        if (!r) r = consumeToken(b, HS_NCOMMENT);
        if (!r) r = consumeToken(b, HS_HADDOCK);
        if (!r) r = consumeToken(b, HS_NHADDOCK);
        if (!r) r = consumeToken(b, HS_NOT_TERMINATED_COMMENT);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // CON_ID
    public static boolean conid(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conid")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_CON_ID);
        exit_section_(b, m, HS_CONID, r);
        return r;
    }

    /* ********************************************************** */
    // constr1 | constr2 | constr3
    public static boolean constr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CONSTR, "<constr>");
        r = constr1(b, l + 1);
        if (!r) r = constr2(b, l + 1);
        if (!r) r = constr3(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // pragma? (onls q_name)? (onls pragma)? onls LEFT_BRACE onl fielddecl? ((onl COMMA)? onl fielddecl)* onl RIGHT_BRACE
    public static boolean constr1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_1, "<constr 1>");
        r = constr1_0(b, l + 1);
        r = r && constr1_1(b, l + 1);
        r = r && constr1_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_BRACE);
        r = r && onl(b, l + 1);
        r = r && constr1_6(b, l + 1);
        r = r && constr1_7(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACE);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // pragma?
    private static boolean constr1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_0")) return false;
        pragma(b, l + 1);
        return true;
    }

    // (onls q_name)?
    private static boolean constr1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_1")) return false;
        constr1_1_0(b, l + 1);
        return true;
    }

    // onls q_name
    private static boolean constr1_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls pragma)?
    private static boolean constr1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_2")) return false;
        constr1_2_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean constr1_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // fielddecl?
    private static boolean constr1_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_6")) return false;
        fielddecl(b, l + 1);
        return true;
    }

    // ((onl COMMA)? onl fielddecl)*
    private static boolean constr1_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_7")) return false;
        while (true) {
            int c = current_position_(b);
            if (!constr1_7_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr1_7", c)) break;
        }
        return true;
    }

    // (onl COMMA)? onl fielddecl
    private static boolean constr1_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = constr1_7_0_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && fielddecl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl COMMA)?
    private static boolean constr1_7_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_7_0_0")) return false;
        constr1_7_0_0_0(b, l + 1);
        return true;
    }

    // onl COMMA
    private static boolean constr1_7_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr1_7_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // pragma? onls (type_signature | ttype | q_name | LEFT_PAREN q_name* RIGHT_PAREN | LEFT_BRACKET q_name* RIGHT_BRACKET)+ (onls pragma onls)? ((onls pragma)? onls ttype (onls pragma)?)*
    public static boolean constr2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_2, "<constr 2>");
        r = constr2_0(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && constr2_2(b, l + 1);
        r = r && constr2_3(b, l + 1);
        r = r && constr2_4(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // pragma?
    private static boolean constr2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_0")) return false;
        pragma(b, l + 1);
        return true;
    }

    // (type_signature | ttype | q_name | LEFT_PAREN q_name* RIGHT_PAREN | LEFT_BRACKET q_name* RIGHT_BRACKET)+
    private static boolean constr2_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = constr2_2_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!constr2_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr2_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // type_signature | ttype | q_name | LEFT_PAREN q_name* RIGHT_PAREN | LEFT_BRACKET q_name* RIGHT_BRACKET
    private static boolean constr2_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_signature(b, l + 1);
        if (!r) r = ttype(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        if (!r) r = constr2_2_0_3(b, l + 1);
        if (!r) r = constr2_2_0_4(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN q_name* RIGHT_PAREN
    private static boolean constr2_2_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2_0_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && constr2_2_0_3_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean constr2_2_0_3_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2_0_3_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr2_2_0_3_1", c)) break;
        }
        return true;
    }

    // LEFT_BRACKET q_name* RIGHT_BRACKET
    private static boolean constr2_2_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2_0_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_BRACKET);
        r = r && constr2_2_0_4_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean constr2_2_0_4_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_2_0_4_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr2_2_0_4_1", c)) break;
        }
        return true;
    }

    // (onls pragma onls)?
    private static boolean constr2_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_3")) return false;
        constr2_3_0(b, l + 1);
        return true;
    }

    // onls pragma onls
    private static boolean constr2_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        r = r && onls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // ((onls pragma)? onls ttype (onls pragma)?)*
    private static boolean constr2_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!constr2_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr2_4", c)) break;
        }
        return true;
    }

    // (onls pragma)? onls ttype (onls pragma)?
    private static boolean constr2_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = constr2_4_0_0(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && constr2_4_0_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls pragma)?
    private static boolean constr2_4_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4_0_0")) return false;
        constr2_4_0_0_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean constr2_4_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls pragma)?
    private static boolean constr2_4_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4_0_3")) return false;
        constr2_4_0_3_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean constr2_4_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr2_4_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (onls pragma? onls ttype)+
    public static boolean constr3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr3")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_3, "<constr 3>");
        r = constr3_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!constr3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constr3", c)) break;
        }
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // onls pragma? onls ttype
    private static boolean constr3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && constr3_0_1(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // pragma?
    private static boolean constr3_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constr3_0_1")) return false;
        pragma(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // constr (onls VERTICAL_BAR onls constr)*
    static boolean constrs(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constrs")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = constr(b, l + 1);
        r = r && constrs_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls VERTICAL_BAR onls constr)*
    private static boolean constrs_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constrs_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!constrs_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "constrs_1", c)) break;
        }
        return true;
    }

    // onls VERTICAL_BAR onls constr
    private static boolean constrs_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "constrs_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_VERTICAL_BAR);
        r = r && onls(b, l + 1);
        r = r && constr(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // CONSYM_ID
    public static boolean consym(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "consym")) return false;
        if (!nextTokenIs(b, HS_CONSYM_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_CONSYM_ID);
        exit_section_(b, m, HS_CONSYM, r);
        return r;
    }

    /* ********************************************************** */
    // DATA (onls pragma)? onls INSTANCE? onls (ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* (onls COLON_COLON ttype)? (onls q_name)* (onls (EQUAL | WHERE))?
    //                                     (onls type_signature (nls type_signature)* | constrs) (onls data_declaration_deriving)? |
    //                                   DATA (onls pragma)? (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls simpletype (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* (onls COLON_COLON ttype)? (onls q_name)* (onls data_declaration_deriving)?
    public static boolean data_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration")) return false;
        if (!nextTokenIs(b, HS_DATA)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = data_declaration_0(b, l + 1);
        if (!r) r = data_declaration_1(b, l + 1);
        exit_section_(b, m, HS_DATA_DECLARATION, r);
        return r;
    }

    // DATA (onls pragma)? onls INSTANCE? onls (ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* (onls COLON_COLON ttype)? (onls q_name)* (onls (EQUAL | WHERE))?
    //                                     (onls type_signature (nls type_signature)* | constrs) (onls data_declaration_deriving)?
    private static boolean data_declaration_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DATA);
        r = r && data_declaration_0_1(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && data_declaration_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && data_declaration_0_5(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && data_declaration_0_8(b, l + 1);
        r = r && data_declaration_0_9(b, l + 1);
        r = r && data_declaration_0_10(b, l + 1);
        r = r && data_declaration_0_11(b, l + 1);
        r = r && data_declaration_0_12(b, l + 1);
        r = r && data_declaration_0_13(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls pragma)?
    private static boolean data_declaration_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_1")) return false;
        data_declaration_0_1_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean data_declaration_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // INSTANCE?
    private static boolean data_declaration_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_3")) return false;
        consumeToken(b, HS_INSTANCE);
        return true;
    }

    // (ccontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean data_declaration_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_5")) return false;
        data_declaration_0_5_0(b, l + 1);
        return true;
    }

    // ccontext onls DOUBLE_RIGHT_ARROW
    private static boolean data_declaration_0_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ccontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean data_declaration_0_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_8")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_0_8_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_0_8", c)) break;
        }
        return true;
    }

    // onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean data_declaration_0_8_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_8_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COLON_COLON ttype)?
    private static boolean data_declaration_0_9(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_9")) return false;
        data_declaration_0_9_0(b, l + 1);
        return true;
    }

    // onls COLON_COLON ttype
    private static boolean data_declaration_0_9_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_9_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls q_name)*
    private static boolean data_declaration_0_10(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_10")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_0_10_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_0_10", c)) break;
        }
        return true;
    }

    // onls q_name
    private static boolean data_declaration_0_10_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_10_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls (EQUAL | WHERE))?
    private static boolean data_declaration_0_11(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_11")) return false;
        data_declaration_0_11_0(b, l + 1);
        return true;
    }

    // onls (EQUAL | WHERE)
    private static boolean data_declaration_0_11_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_11_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && data_declaration_0_11_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // EQUAL | WHERE
    private static boolean data_declaration_0_11_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_11_0_1")) return false;
        boolean r;
        r = consumeToken(b, HS_EQUAL);
        if (!r) r = consumeToken(b, HS_WHERE);
        return r;
    }

    // onls type_signature (nls type_signature)* | constrs
    private static boolean data_declaration_0_12(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_12")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = data_declaration_0_12_0(b, l + 1);
        if (!r) r = constrs(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onls type_signature (nls type_signature)*
    private static boolean data_declaration_0_12_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_12_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && type_signature(b, l + 1);
        r = r && data_declaration_0_12_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (nls type_signature)*
    private static boolean data_declaration_0_12_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_12_0_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_0_12_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_0_12_0_2", c)) break;
        }
        return true;
    }

    // nls type_signature
    private static boolean data_declaration_0_12_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_12_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls(b, l + 1);
        r = r && type_signature(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls data_declaration_deriving)?
    private static boolean data_declaration_0_13(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_13")) return false;
        data_declaration_0_13_0(b, l + 1);
        return true;
    }

    // onls data_declaration_deriving
    private static boolean data_declaration_0_13_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_0_13_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && data_declaration_deriving(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DATA (onls pragma)? (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls simpletype (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* (onls COLON_COLON ttype)? (onls q_name)* (onls data_declaration_deriving)?
    private static boolean data_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DATA);
        r = r && data_declaration_1_1(b, l + 1);
        r = r && data_declaration_1_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && data_declaration_1_5(b, l + 1);
        r = r && data_declaration_1_6(b, l + 1);
        r = r && data_declaration_1_7(b, l + 1);
        r = r && data_declaration_1_8(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls pragma)?
    private static boolean data_declaration_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_1")) return false;
        data_declaration_1_1_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean data_declaration_1_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean data_declaration_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_1_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_1_2", c)) break;
        }
        return true;
    }

    // onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean data_declaration_1_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean data_declaration_1_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_5")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_1_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_1_5", c)) break;
        }
        return true;
    }

    // onls LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean data_declaration_1_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COLON_COLON ttype)?
    private static boolean data_declaration_1_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_6")) return false;
        data_declaration_1_6_0(b, l + 1);
        return true;
    }

    // onls COLON_COLON ttype
    private static boolean data_declaration_1_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls q_name)*
    private static boolean data_declaration_1_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_7")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_1_7_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_1_7", c)) break;
        }
        return true;
    }

    // onls q_name
    private static boolean data_declaration_1_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls data_declaration_deriving)?
    private static boolean data_declaration_1_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_8")) return false;
        data_declaration_1_8_0(b, l + 1);
        return true;
    }

    // onls data_declaration_deriving
    private static boolean data_declaration_1_8_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_1_8_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && data_declaration_deriving(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (onl DERIVING onl ttype | onl DERIVING onl LEFT_PAREN ttype (onl COMMA onl ttype)* onl RIGHT_PAREN)+
    public static boolean data_declaration_deriving(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_DATA_DECLARATION_DERIVING, "<data declaration deriving>");
        r = data_declaration_deriving_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!data_declaration_deriving_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_deriving", c)) break;
        }
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // onl DERIVING onl ttype | onl DERIVING onl LEFT_PAREN ttype (onl COMMA onl ttype)* onl RIGHT_PAREN
    private static boolean data_declaration_deriving_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = data_declaration_deriving_0_0(b, l + 1);
        if (!r) r = data_declaration_deriving_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onl DERIVING onl ttype
    private static boolean data_declaration_deriving_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_DERIVING);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onl DERIVING onl LEFT_PAREN ttype (onl COMMA onl ttype)* onl RIGHT_PAREN
    private static boolean data_declaration_deriving_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_DERIVING);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && ttype(b, l + 1);
        r = r && data_declaration_deriving_0_1_5(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl COMMA onl ttype)*
    private static boolean data_declaration_deriving_0_1_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_5")) return false;
        while (true) {
            int c = current_position_(b);
            if (!data_declaration_deriving_0_1_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "data_declaration_deriving_0_1_5", c)) break;
        }
        return true;
    }

    // onl COMMA onl ttype
    private static boolean data_declaration_deriving_0_1_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // DEFAULT onls (type_signature | LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN)
    public static boolean default_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration")) return false;
        if (!nextTokenIs(b, HS_DEFAULT)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DEFAULT);
        r = r && onls(b, l + 1);
        r = r && default_declaration_2(b, l + 1);
        exit_section_(b, m, HS_DEFAULT_DECLARATION, r);
        return r;
    }

    // type_signature | LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN
    private static boolean default_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_signature(b, l + 1);
        if (!r) r = default_declaration_2_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN
    private static boolean default_declaration_2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && default_declaration_2_1_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (ttype (COMMA ttype)*)?
    private static boolean default_declaration_2_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2_1_1")) return false;
        default_declaration_2_1_1_0(b, l + 1);
        return true;
    }

    // ttype (COMMA ttype)*
    private static boolean default_declaration_2_1_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2_1_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ttype(b, l + 1);
        r = r && default_declaration_2_1_1_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (COMMA ttype)*
    private static boolean default_declaration_2_1_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2_1_1_0_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!default_declaration_2_1_1_0_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "default_declaration_2_1_1_0_1", c)) break;
        }
        return true;
    }

    // COMMA ttype
    private static boolean default_declaration_2_1_1_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "default_declaration_2_1_1_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COMMA);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // DERIVING INSTANCE (scontext onls DOUBLE_RIGHT_ARROW)? onls q_name onls inst
    public static boolean deriving_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "deriving_declaration")) return false;
        if (!nextTokenIs(b, HS_DERIVING)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_DERIVING, HS_INSTANCE);
        r = r && deriving_declaration_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && q_name(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && inst(b, l + 1);
        exit_section_(b, m, HS_DERIVING_DECLARATION, r);
        return r;
    }

    // (scontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean deriving_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "deriving_declaration_2")) return false;
        deriving_declaration_2_0(b, l + 1);
        return true;
    }

    // scontext onls DOUBLE_RIGHT_ARROW
    private static boolean deriving_declaration_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "deriving_declaration_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = scontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // "via"
    public static boolean deriving_via(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "deriving_via")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_DERIVING_VIA, "<deriving via>");
        r = consumeToken(b, "via");
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // expression SEMICOLON
    static boolean do_definition(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "do_definition")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = expression(b, l + 1);
        r = r && consumeToken(b, HS_SEMICOLON);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // DO LEFT_BRACE do_definition* expression? RIGHT_BRACE?
    public static boolean do_notation_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "do_notation_expression")) return false;
        if (!nextTokenIs(b, HS_DO)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_DO_NOTATION_EXPRESSION, null);
        r = consumeTokens(b, 1, HS_DO, HS_LEFT_BRACE);
        p = r; // pin = 1
        r = r && report_error_(b, do_notation_expression_2(b, l + 1));
        r = p && report_error_(b, do_notation_expression_3(b, l + 1)) && r;
        r = p && do_notation_expression_4(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // do_definition*
    private static boolean do_notation_expression_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "do_notation_expression_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!do_definition(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "do_notation_expression_2", c)) break;
        }
        return true;
    }

    // expression?
    private static boolean do_notation_expression_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "do_notation_expression_3")) return false;
        expression(b, l + 1);
        return true;
    }

    // RIGHT_BRACE?
    private static boolean do_notation_expression_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "do_notation_expression_4")) return false;
        consumeToken(b, HS_RIGHT_BRACE);
        return true;
    }

    /* ********************************************************** */
    // DOT DOT
    public static boolean dot_dot(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "dot_dot")) return false;
        if (!nextTokenIs(b, HS_DOT)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_DOT, HS_DOT);
        exit_section_(b, m, HS_DOT_DOT, r);
        return r;
    }

    /* ********************************************************** */
    // export1 | export2 | export3
    public static boolean export(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_EXPORT, "<export>");
        r = export1(b, l + 1);
        if (!r) r = export2(b, l + 1);
        if (!r) r = export3(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // MODULE modid
    static boolean export1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export1")) return false;
        if (!nextTokenIs(b, HS_MODULE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_MODULE);
        r = r && modid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_name (onl LEFT_PAREN (onl (dot_dot | q_name) (onl COMMA onl q_name)*)? onl RIGHT_PAREN)?
    static boolean export2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && export2_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl LEFT_PAREN (onl (dot_dot | q_name) (onl COMMA onl q_name)*)? onl RIGHT_PAREN)?
    private static boolean export2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1")) return false;
        export2_1_0(b, l + 1);
        return true;
    }

    // onl LEFT_PAREN (onl (dot_dot | q_name) (onl COMMA onl q_name)*)? onl RIGHT_PAREN
    private static boolean export2_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && export2_1_0_2(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl (dot_dot | q_name) (onl COMMA onl q_name)*)?
    private static boolean export2_1_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0_2")) return false;
        export2_1_0_2_0(b, l + 1);
        return true;
    }

    // onl (dot_dot | q_name) (onl COMMA onl q_name)*
    private static boolean export2_1_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && export2_1_0_2_0_1(b, l + 1);
        r = r && export2_1_0_2_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // dot_dot | q_name
    private static boolean export2_1_0_2_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0_2_0_1")) return false;
        boolean r;
        r = dot_dot(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        return r;
    }

    // (onl COMMA onl q_name)*
    private static boolean export2_1_0_2_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0_2_0_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!export2_1_0_2_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "export2_1_0_2_0_2", c)) break;
        }
        return true;
    }

    // onl COMMA onl q_name
    private static boolean export2_1_0_2_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export2_1_0_2_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onl(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // TYPE onl q_name
    static boolean export3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "export3")) return false;
        if (!nextTokenIs(b, HS_TYPE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onl(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_PAREN ((onl COMMA)? onl export)* (onl COMMA)? onl RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN
    public static boolean exports(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports")) return false;
        if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = exports_0(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        exit_section_(b, m, HS_EXPORTS, r);
        return r;
    }

    // LEFT_PAREN ((onl COMMA)? onl export)* (onl COMMA)? onl RIGHT_PAREN
    private static boolean exports_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && exports_0_1(b, l + 1);
        r = r && exports_0_2(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // ((onl COMMA)? onl export)*
    private static boolean exports_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!exports_0_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "exports_0_1", c)) break;
        }
        return true;
    }

    // (onl COMMA)? onl export
    private static boolean exports_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = exports_0_1_0_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && export(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl COMMA)?
    private static boolean exports_0_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_1_0_0")) return false;
        exports_0_1_0_0_0(b, l + 1);
        return true;
    }

    // onl COMMA
    private static boolean exports_0_1_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_1_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl COMMA)?
    private static boolean exports_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_2")) return false;
        exports_0_2_0(b, l + 1);
        return true;
    }

    // onl COMMA
    private static boolean exports_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "exports_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // let_abstraction | do_notation_expression | if_expression | application_expression | case_of_expression | atom | line_expression (nls line_expression)*
    public static boolean expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "expression")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, HS_EXPRESSION, "<expression>");
        r = let_abstraction(b, l + 1);
        if (!r) r = do_notation_expression(b, l + 1);
        if (!r) r = if_expression(b, l + 1);
        if (!r) r = application_expression(b, l + 1);
        if (!r) r = case_of_expression(b, l + 1);
        if (!r) r = atom(b, l + 1);
        if (!r) r = expression_6(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // line_expression (nls line_expression)*
    private static boolean expression_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "expression_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = line_expression(b, l + 1);
        r = r && expression_6_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (nls line_expression)*
    private static boolean expression_6_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "expression_6_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!expression_6_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "expression_6_1", c)) break;
        }
        return true;
    }

    // nls line_expression
    private static boolean expression_6_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "expression_6_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls(b, l + 1);
        r = r && line_expression(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_names (onl COLON_COLON pragma? (onls ("forall" | FORALL) (onls q_name)+)? (onls scontext onls DOUBLE_RIGHT_ARROW)? pragma? onls ttype)?
    public static boolean fielddecl(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_FIELDDECL, "<fielddecl>");
        r = q_names(b, l + 1);
        r = r && fielddecl_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (onl COLON_COLON pragma? (onls ("forall" | FORALL) (onls q_name)+)? (onls scontext onls DOUBLE_RIGHT_ARROW)? pragma? onls ttype)?
    private static boolean fielddecl_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1")) return false;
        fielddecl_1_0(b, l + 1);
        return true;
    }

    // onl COLON_COLON pragma? (onls ("forall" | FORALL) (onls q_name)+)? (onls scontext onls DOUBLE_RIGHT_ARROW)? pragma? onls ttype
    private static boolean fielddecl_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && fielddecl_1_0_2(b, l + 1);
        r = r && fielddecl_1_0_3(b, l + 1);
        r = r && fielddecl_1_0_4(b, l + 1);
        r = r && fielddecl_1_0_5(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // pragma?
    private static boolean fielddecl_1_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_2")) return false;
        pragma(b, l + 1);
        return true;
    }

    // (onls ("forall" | FORALL) (onls q_name)+)?
    private static boolean fielddecl_1_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_3")) return false;
        fielddecl_1_0_3_0(b, l + 1);
        return true;
    }

    // onls ("forall" | FORALL) (onls q_name)+
    private static boolean fielddecl_1_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && fielddecl_1_0_3_0_1(b, l + 1);
        r = r && fielddecl_1_0_3_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // "forall" | FORALL
    private static boolean fielddecl_1_0_3_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_3_0_1")) return false;
        boolean r;
        r = consumeToken(b, "forall");
        if (!r) r = consumeToken(b, HS_FORALL);
        return r;
    }

    // (onls q_name)+
    private static boolean fielddecl_1_0_3_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_3_0_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = fielddecl_1_0_3_0_2_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!fielddecl_1_0_3_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "fielddecl_1_0_3_0_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // onls q_name
    private static boolean fielddecl_1_0_3_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_3_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls scontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean fielddecl_1_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_4")) return false;
        fielddecl_1_0_4_0(b, l + 1);
        return true;
    }

    // onls scontext onls DOUBLE_RIGHT_ARROW
    private static boolean fielddecl_1_0_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && scontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // pragma?
    private static boolean fielddecl_1_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fielddecl_1_0_5")) return false;
        pragma(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // (pragma | INCLUDE_DIRECTIVE | DIRECTIVE)? (NEWLINE* (pragma | INCLUDE_DIRECTIVE | DIRECTIVE))*
    public static boolean file_header(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_FILE_HEADER, "<file header>");
        r = file_header_0(b, l + 1);
        r = r && file_header_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (pragma | INCLUDE_DIRECTIVE | DIRECTIVE)?
    private static boolean file_header_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_0")) return false;
        file_header_0_0(b, l + 1);
        return true;
    }

    // pragma | INCLUDE_DIRECTIVE | DIRECTIVE
    private static boolean file_header_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_0_0")) return false;
        boolean r;
        r = pragma(b, l + 1);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_DIRECTIVE);
        return r;
    }

    // (NEWLINE* (pragma | INCLUDE_DIRECTIVE | DIRECTIVE))*
    private static boolean file_header_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!file_header_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "file_header_1", c)) break;
        }
        return true;
    }

    // NEWLINE* (pragma | INCLUDE_DIRECTIVE | DIRECTIVE)
    private static boolean file_header_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = file_header_1_0_0(b, l + 1);
        r = r && file_header_1_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // NEWLINE*
    private static boolean file_header_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_1_0_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_NEWLINE)) break;
            if (!empty_element_parsed_guard_(b, "file_header_1_0_0", c)) break;
        }
        return true;
    }

    // pragma | INCLUDE_DIRECTIVE | DIRECTIVE
    private static boolean file_header_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "file_header_1_0_1")) return false;
        boolean r;
        r = pragma(b, l + 1);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_DIRECTIVE);
        return r;
    }

    /* ********************************************************** */
    // INFIXL | INFIXR | INFIX
    static boolean fixity(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fixity")) return false;
        boolean r;
        r = consumeToken(b, HS_INFIXL);
        if (!r) r = consumeToken(b, HS_INFIXR);
        if (!r) r = consumeToken(b, HS_INFIX);
        return r;
    }

    /* ********************************************************** */
    // fixity (DECIMAL)? q_names
    public static boolean fixity_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fixity_declaration")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_FIXITY_DECLARATION, "<fixity declaration>");
        r = fixity(b, l + 1);
        r = r && fixity_declaration_1(b, l + 1);
        r = r && q_names(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (DECIMAL)?
    private static boolean fixity_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "fixity_declaration_1")) return false;
        consumeToken(b, HS_DECIMAL);
        return true;
    }

    /* ********************************************************** */
    // (FOREIGN_IMPORT | FOREIGN_EXPORT) onls expression
    public static boolean foreign_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "foreign_declaration")) return false;
        if (!nextTokenIs(b, "<foreign declaration>", HS_FOREIGN_EXPORT, HS_FOREIGN_IMPORT)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_FOREIGN_DECLARATION, "<foreign declaration>");
        r = foreign_declaration_0(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && expression(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // FOREIGN_IMPORT | FOREIGN_EXPORT
    private static boolean foreign_declaration_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "foreign_declaration_0")) return false;
        boolean r;
        r = consumeToken(b, HS_FOREIGN_IMPORT);
        if (!r) r = consumeToken(b, HS_FOREIGN_EXPORT);
        return r;
    }

    /* ********************************************************** */
    // quasi_quote | q_name | symbol_reserved_op | reserved_id | LEFT_PAREN | RIGHT_PAREN | FLOAT |
    //                                   SEMICOLON | LEFT_BRACKET | RIGHT_BRACKET | literal | LEFT_BRACE | RIGHT_BRACE |
    //                                   COMMA | QUOTE | BACKQUOTE | fixity |
    //                                   pragma | DIRECTIVE | DOUBLE_QUOTES
    static boolean general_id(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "general_id")) return false;
        boolean r;
        r = quasi_quote(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        if (!r) r = symbol_reserved_op(b, l + 1);
        if (!r) r = reserved_id(b, l + 1);
        if (!r) r = consumeToken(b, HS_LEFT_PAREN);
        if (!r) r = consumeToken(b, HS_RIGHT_PAREN);
        if (!r) r = consumeToken(b, HS_FLOAT);
        if (!r) r = consumeToken(b, HS_SEMICOLON);
        if (!r) r = consumeToken(b, HS_LEFT_BRACKET);
        if (!r) r = consumeToken(b, HS_RIGHT_BRACKET);
        if (!r) r = literal(b, l + 1);
        if (!r) r = consumeToken(b, HS_LEFT_BRACE);
        if (!r) r = consumeToken(b, HS_RIGHT_BRACE);
        if (!r) r = consumeToken(b, HS_COMMA);
        if (!r) r = consumeToken(b, HS_QUOTE);
        if (!r) r = consumeToken(b, HS_BACKQUOTE);
        if (!r) r = fixity(b, l + 1);
        if (!r) r = pragma(b, l + 1);
        if (!r) r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_DOUBLE_QUOTES);
        return r;
    }

    /* ********************************************************** */
    // ONE_PRAGMA | PRAGMA_SEP | CHARACTER_LITERAL | STRING_LITERAL | NEWLINE | DASH | HASH
    public static boolean general_pragma_content(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "general_pragma_content")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_GENERAL_PRAGMA_CONTENT, "<general pragma content>");
        r = consumeToken(b, HS_ONE_PRAGMA);
        if (!r) r = consumeToken(b, HS_PRAGMA_SEP);
        if (!r) r = consumeToken(b, HS_CHARACTER_LITERAL);
        if (!r) r = consumeToken(b, HS_STRING_LITERAL);
        if (!r) r = consumeToken(b, HS_NEWLINE);
        if (!r) r = consumeToken(b, HS_DASH);
        if (!r) r = consumeToken(b, HS_HASH);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // QUOTE? q_name | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN | QUOTE? LEFT_BRACKET RIGHT_BRACKET | LEFT_PAREN COMMA (COMMA)* RIGHT_PAREN
    public static boolean gtycon(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_GTYCON, "<gtycon>");
        r = gtycon_0(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_ARROW, HS_RIGHT_PAREN);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        if (!r) r = gtycon_3(b, l + 1);
        if (!r) r = gtycon_4(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // QUOTE? q_name
    private static boolean gtycon_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = gtycon_0_0(b, l + 1);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean gtycon_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_0_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // QUOTE? LEFT_BRACKET RIGHT_BRACKET
    private static boolean gtycon_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = gtycon_3_0(b, l + 1);
        r = r && consumeTokens(b, 0, HS_LEFT_BRACKET, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean gtycon_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_3_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // LEFT_PAREN COMMA (COMMA)* RIGHT_PAREN
    private static boolean gtycon_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_LEFT_PAREN, HS_COMMA);
        r = r && gtycon_4_2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (COMMA)*
    private static boolean gtycon_4_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "gtycon_4_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_COMMA)) break;
            if (!empty_element_parsed_guard_(b, "gtycon_4_2", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // IF expression THEN expression ELSE expression
    public static boolean if_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "if_expression")) return false;
        if (!nextTokenIs(b, HS_IF)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_IF_EXPRESSION, null);
        r = consumeToken(b, HS_IF);
        p = r; // pin = 1
        r = r && report_error_(b, expression(b, l + 1));
        r = p && report_error_(b, consumeToken(b, HS_THEN)) && r;
        r = p && report_error_(b, expression(b, l + 1)) && r;
        r = p && report_error_(b, consumeToken(b, HS_ELSE)) && r;
        r = p && expression(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // expression EQUAL expression
    public static boolean implementation_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "implementation_declaration")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPLEMENTATION_DECLARATION, "<implementation declaration>");
        r = expression(b, l + 1);
        r = r && consumeToken(b, HS_EQUAL);
        p = r; // pin = 2
        r = r && expression(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // IMPORT (onls pragma)? (onls import_qualified)? (onls import_package_name)? onls modid (onls import_qualified_as)? (onls import_spec)? NEWLINE?
    public static boolean import_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration")) return false;
        if (!nextTokenIs(b, HS_IMPORT)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_DECLARATION, null);
        r = consumeToken(b, HS_IMPORT);
        p = r; // pin = 1
        r = r && report_error_(b, import_declaration_1(b, l + 1));
        r = p && report_error_(b, import_declaration_2(b, l + 1)) && r;
        r = p && report_error_(b, import_declaration_3(b, l + 1)) && r;
        r = p && report_error_(b, onls(b, l + 1)) && r;
        r = p && report_error_(b, modid(b, l + 1)) && r;
        r = p && report_error_(b, import_declaration_6(b, l + 1)) && r;
        r = p && report_error_(b, import_declaration_7(b, l + 1)) && r;
        r = p && import_declaration_8(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // (onls pragma)?
    private static boolean import_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_1")) return false;
        import_declaration_1_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean import_declaration_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls import_qualified)?
    private static boolean import_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_2")) return false;
        import_declaration_2_0(b, l + 1);
        return true;
    }

    // onls import_qualified
    private static boolean import_declaration_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && import_qualified(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls import_package_name)?
    private static boolean import_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_3")) return false;
        import_declaration_3_0(b, l + 1);
        return true;
    }

    // onls import_package_name
    private static boolean import_declaration_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && import_package_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls import_qualified_as)?
    private static boolean import_declaration_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_6")) return false;
        import_declaration_6_0(b, l + 1);
        return true;
    }

    // onls import_qualified_as
    private static boolean import_declaration_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && import_qualified_as(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls import_spec)?
    private static boolean import_declaration_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_7")) return false;
        import_declaration_7_0(b, l + 1);
        return true;
    }

    // onls import_spec
    private static boolean import_declaration_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && import_spec(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // NEWLINE?
    private static boolean import_declaration_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declaration_8")) return false;
        consumeToken(b, HS_NEWLINE);
        return true;
    }

    /* ********************************************************** */
    // import_declaration (onl import_declaration)* (DIRECTIVE | INCLUDE_DIRECTIVE)*
    public static boolean import_declarations(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declarations")) return false;
        if (!nextTokenIs(b, HS_IMPORT)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = import_declaration(b, l + 1);
        r = r && import_declarations_1(b, l + 1);
        r = r && import_declarations_2(b, l + 1);
        exit_section_(b, m, HS_IMPORT_DECLARATIONS, r);
        return r;
    }

    // (onl import_declaration)*
    private static boolean import_declarations_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declarations_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!import_declarations_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "import_declarations_1", c)) break;
        }
        return true;
    }

    // onl import_declaration
    private static boolean import_declarations_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declarations_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && import_declaration(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (DIRECTIVE | INCLUDE_DIRECTIVE)*
    private static boolean import_declarations_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declarations_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!import_declarations_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "import_declarations_2", c)) break;
        }
        return true;
    }

    // DIRECTIVE | INCLUDE_DIRECTIVE
    private static boolean import_declarations_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_declarations_2_0")) return false;
        boolean r;
        r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        return r;
    }

    /* ********************************************************** */
    // LEFT_PAREN RIGHT_PAREN
    public static boolean import_empty_spec(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_empty_spec")) return false;
        if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        exit_section_(b, m, HS_IMPORT_EMPTY_SPEC, r);
        return r;
    }

    /* ********************************************************** */
    // "hiding"
    public static boolean import_hiding(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_HIDING, "<import hiding>");
        r = consumeToken(b, "hiding");
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // import_hiding onls LEFT_PAREN onls (import_id onls (onls COMMA onls import_id)* onls (COMMA)?)? onls RIGHT_PAREN
    public static boolean import_hiding_spec(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_HIDING_SPEC, "<import hiding spec>");
        r = import_hiding(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && import_hiding_spec_4(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (import_id onls (onls COMMA onls import_id)* onls (COMMA)?)?
    private static boolean import_hiding_spec_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec_4")) return false;
        import_hiding_spec_4_0(b, l + 1);
        return true;
    }

    // import_id onls (onls COMMA onls import_id)* onls (COMMA)?
    private static boolean import_hiding_spec_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = import_id(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && import_hiding_spec_4_0_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && import_hiding_spec_4_0_4(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls import_id)*
    private static boolean import_hiding_spec_4_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec_4_0_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!import_hiding_spec_4_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "import_hiding_spec_4_0_2", c)) break;
        }
        return true;
    }

    // onls COMMA onls import_id
    private static boolean import_hiding_spec_4_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec_4_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && import_id(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (COMMA)?
    private static boolean import_hiding_spec_4_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_hiding_spec_4_0_4")) return false;
        consumeToken(b, HS_COMMA);
        return true;
    }

    /* ********************************************************** */
    // TYPE? (q_name LEFT_PAREN onls dot_dot onls RIGHT_PAREN | q_name onls LEFT_PAREN onls (q_name onls (COMMA onls q_name onls)* onls)? RIGHT_PAREN | q_name)
    public static boolean import_id(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_ID, "<import id>");
        r = import_id_0(b, l + 1);
        r = r && import_id_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // TYPE?
    private static boolean import_id_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_0")) return false;
        consumeToken(b, HS_TYPE);
        return true;
    }

    // q_name LEFT_PAREN onls dot_dot onls RIGHT_PAREN | q_name onls LEFT_PAREN onls (q_name onls (COMMA onls q_name onls)* onls)? RIGHT_PAREN | q_name
    private static boolean import_id_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = import_id_1_0(b, l + 1);
        if (!r) r = import_id_1_1(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name LEFT_PAREN onls dot_dot onls RIGHT_PAREN
    private static boolean import_id_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && dot_dot(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name onls LEFT_PAREN onls (q_name onls (COMMA onls q_name onls)* onls)? RIGHT_PAREN
    private static boolean import_id_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && import_id_1_1_4(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (q_name onls (COMMA onls q_name onls)* onls)?
    private static boolean import_id_1_1_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_1_4")) return false;
        import_id_1_1_4_0(b, l + 1);
        return true;
    }

    // q_name onls (COMMA onls q_name onls)* onls
    private static boolean import_id_1_1_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_1_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && import_id_1_1_4_0_2(b, l + 1);
        r = r && onls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (COMMA onls q_name onls)*
    private static boolean import_id_1_1_4_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_1_4_0_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!import_id_1_1_4_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "import_id_1_1_4_0_2", c)) break;
        }
        return true;
    }

    // COMMA onls q_name onls
    private static boolean import_id_1_1_4_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_id_1_1_4_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && q_name(b, l + 1);
        r = r && onls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_PAREN onls import_id (onls COMMA? onls import_id)* onls (COMMA)? onls RIGHT_PAREN
    public static boolean import_ids_spec(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_ids_spec")) return false;
        if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && import_id(b, l + 1);
        r = r && import_ids_spec_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && import_ids_spec_5(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, HS_IMPORT_IDS_SPEC, r);
        return r;
    }

    // (onls COMMA? onls import_id)*
    private static boolean import_ids_spec_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_ids_spec_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!import_ids_spec_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "import_ids_spec_3", c)) break;
        }
        return true;
    }

    // onls COMMA? onls import_id
    private static boolean import_ids_spec_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_ids_spec_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && import_ids_spec_3_0_1(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && import_id(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // COMMA?
    private static boolean import_ids_spec_3_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_ids_spec_3_0_1")) return false;
        consumeToken(b, HS_COMMA);
        return true;
    }

    // (COMMA)?
    private static boolean import_ids_spec_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_ids_spec_5")) return false;
        consumeToken(b, HS_COMMA);
        return true;
    }

    /* ********************************************************** */
    // text_literal
    public static boolean import_package_name(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_package_name")) return false;
        if (!nextTokenIs(b, HS_STRING_LITERAL)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = text_literal(b, l + 1);
        exit_section_(b, m, HS_IMPORT_PACKAGE_NAME, r);
        return r;
    }

    /* ********************************************************** */
    // "qualified"
    public static boolean import_qualified(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_qualified")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_QUALIFIED, "<import qualified>");
        r = consumeToken(b, "qualified");
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // "as" qualifier
    public static boolean import_qualified_as(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_qualified_as")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_QUALIFIED_AS, "<import qualified as>");
        r = consumeToken(b, "as");
        r = r && qualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // import_ids_spec |
    //                                   import_hiding_spec |
    //                                   import_empty_spec
    public static boolean import_spec(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "import_spec")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_SPEC, "<import spec>");
        r = import_ids_spec(b, l + 1);
        if (!r) r = import_hiding_spec(b, l + 1);
        if (!r) r = import_empty_spec(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // gtycon+ instvar* (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)* instvar* |
    //                                   (LEFT_PAREN onls instvar (onls COMMA onls instvar)* onls RIGHT_PAREN)+ instvar* |
    //                                   QUOTE? (LEFT_BRACKET onls instvar onls RIGHT_BRACKET)+ instvar* |
    //                                   (LEFT_PAREN onls instvar+ onls RIGHT_PAREN)+ instvar* |
    //                                   ((LEFT_PAREN onls instvar+ (onls RIGHT_ARROW onls instvar+)+ onls RIGHT_PAREN)+ instvar*)+
    public static boolean inst(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_INST, "<inst>");
        r = inst_0(b, l + 1);
        if (!r) r = inst_1(b, l + 1);
        if (!r) r = inst_2(b, l + 1);
        if (!r) r = inst_3(b, l + 1);
        if (!r) r = inst_4(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // gtycon+ instvar* (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)* instvar*
    private static boolean inst_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_0_0(b, l + 1);
        r = r && inst_0_1(b, l + 1);
        r = r && inst_0_2(b, l + 1);
        r = r && inst_0_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // gtycon+
    private static boolean inst_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = gtycon(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!gtycon(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_0_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_0_1", c)) break;
        }
        return true;
    }

    // (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)*
    private static boolean inst_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inst_0_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_0_2", c)) break;
        }
        return true;
    }

    // LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN
    private static boolean inst_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && gtycon(b, l + 1);
        r = r && inst_0_2_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (instvar)*
    private static boolean inst_0_2_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_2_0_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inst_0_2_0_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_0_2_0_3", c)) break;
        }
        return true;
    }

    // (instvar)
    private static boolean inst_0_2_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_2_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = instvar(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_0_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_0_3", c)) break;
        }
        return true;
    }

    // (LEFT_PAREN onls instvar (onls COMMA onls instvar)* onls RIGHT_PAREN)+ instvar*
    private static boolean inst_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_1_0(b, l + 1);
        r = r && inst_1_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (LEFT_PAREN onls instvar (onls COMMA onls instvar)* onls RIGHT_PAREN)+
    private static boolean inst_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_1_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_1_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_1_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN onls instvar (onls COMMA onls instvar)* onls RIGHT_PAREN
    private static boolean inst_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && instvar(b, l + 1);
        r = r && inst_1_0_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls instvar)*
    private static boolean inst_1_0_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1_0_0_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inst_1_0_0_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_1_0_0_3", c)) break;
        }
        return true;
    }

    // onls COMMA onls instvar
    private static boolean inst_1_0_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1_0_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && instvar(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_1_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_1_1", c)) break;
        }
        return true;
    }

    // QUOTE? (LEFT_BRACKET onls instvar onls RIGHT_BRACKET)+ instvar*
    private static boolean inst_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_2_0(b, l + 1);
        r = r && inst_2_1(b, l + 1);
        r = r && inst_2_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // QUOTE?
    private static boolean inst_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_2_0")) return false;
        consumeToken(b, HS_QUOTE);
        return true;
    }

    // (LEFT_BRACKET onls instvar onls RIGHT_BRACKET)+
    private static boolean inst_2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_2_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_2_1_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_2_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_2_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_BRACKET onls instvar onls RIGHT_BRACKET
    private static boolean inst_2_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_2_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_BRACKET);
        r = r && onls(b, l + 1);
        r = r && instvar(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_2_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_2_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_2_2", c)) break;
        }
        return true;
    }

    // (LEFT_PAREN onls instvar+ onls RIGHT_PAREN)+ instvar*
    private static boolean inst_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_3_0(b, l + 1);
        r = r && inst_3_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (LEFT_PAREN onls instvar+ onls RIGHT_PAREN)+
    private static boolean inst_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_3_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_3_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_3_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN onls instvar+ onls RIGHT_PAREN
    private static boolean inst_3_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_3_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && inst_3_0_0_2(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar+
    private static boolean inst_3_0_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_3_0_0_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = instvar(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_3_0_0_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_3_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_3_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_3_1", c)) break;
        }
        return true;
    }

    // ((LEFT_PAREN onls instvar+ (onls RIGHT_ARROW onls instvar+)+ onls RIGHT_PAREN)+ instvar*)+
    private static boolean inst_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_4_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (LEFT_PAREN onls instvar+ (onls RIGHT_ARROW onls instvar+)+ onls RIGHT_PAREN)+ instvar*
    private static boolean inst_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_4_0_0(b, l + 1);
        r = r && inst_4_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (LEFT_PAREN onls instvar+ (onls RIGHT_ARROW onls instvar+)+ onls RIGHT_PAREN)+
    private static boolean inst_4_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_4_0_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_4_0_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4_0_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN onls instvar+ (onls RIGHT_ARROW onls instvar+)+ onls RIGHT_PAREN
    private static boolean inst_4_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && inst_4_0_0_0_2(b, l + 1);
        r = r && inst_4_0_0_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar+
    private static boolean inst_4_0_0_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0_0_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = instvar(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4_0_0_0_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls RIGHT_ARROW onls instvar+)+
    private static boolean inst_4_0_0_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0_0_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inst_4_0_0_0_3_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!inst_4_0_0_0_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4_0_0_0_3", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // onls RIGHT_ARROW onls instvar+
    private static boolean inst_4_0_0_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_ARROW);
        r = r && onls(b, l + 1);
        r = r && inst_4_0_0_0_3_0_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar+
    private static boolean inst_4_0_0_0_3_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_0_0_3_0_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = instvar(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4_0_0_0_3_0_3", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // instvar*
    private static boolean inst_4_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inst_4_0_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!instvar(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inst_4_0_1", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // INSTANCE (onls "OVERLAPPABLE_" | "OVERLAPPING_" | pragma)? (onls varid+ DOT)? (onls scontext onls DOUBLE_RIGHT_ARROW)? onls
    //                                     (type_equality | q_name onls inst (onls WHERE (onls cidecls)?)?)
    public static boolean instance_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration")) return false;
        if (!nextTokenIs(b, HS_INSTANCE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_INSTANCE);
        r = r && instance_declaration_1(b, l + 1);
        r = r && instance_declaration_2(b, l + 1);
        r = r && instance_declaration_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && instance_declaration_5(b, l + 1);
        exit_section_(b, m, HS_INSTANCE_DECLARATION, r);
        return r;
    }

    // (onls "OVERLAPPABLE_" | "OVERLAPPING_" | pragma)?
    private static boolean instance_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_1")) return false;
        instance_declaration_1_0(b, l + 1);
        return true;
    }

    // onls "OVERLAPPABLE_" | "OVERLAPPING_" | pragma
    private static boolean instance_declaration_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = instance_declaration_1_0_0(b, l + 1);
        if (!r) r = consumeToken(b, "OVERLAPPING_");
        if (!r) r = pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onls "OVERLAPPABLE_"
    private static boolean instance_declaration_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_1_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, "OVERLAPPABLE_");
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls varid+ DOT)?
    private static boolean instance_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_2")) return false;
        instance_declaration_2_0(b, l + 1);
        return true;
    }

    // onls varid+ DOT
    private static boolean instance_declaration_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && instance_declaration_2_0_1(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        exit_section_(b, m, null, r);
        return r;
    }

    // varid+
    private static boolean instance_declaration_2_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_2_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varid(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!varid(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "instance_declaration_2_0_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls scontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean instance_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_3")) return false;
        instance_declaration_3_0(b, l + 1);
        return true;
    }

    // onls scontext onls DOUBLE_RIGHT_ARROW
    private static boolean instance_declaration_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && scontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // type_equality | q_name onls inst (onls WHERE (onls cidecls)?)?
    private static boolean instance_declaration_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_equality(b, l + 1);
        if (!r) r = instance_declaration_5_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name onls inst (onls WHERE (onls cidecls)?)?
    private static boolean instance_declaration_5_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && inst(b, l + 1);
        r = r && instance_declaration_5_1_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls WHERE (onls cidecls)?)?
    private static boolean instance_declaration_5_1_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5_1_3")) return false;
        instance_declaration_5_1_3_0(b, l + 1);
        return true;
    }

    // onls WHERE (onls cidecls)?
    private static boolean instance_declaration_5_1_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5_1_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_WHERE);
        r = r && instance_declaration_5_1_3_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls cidecls)?
    private static boolean instance_declaration_5_1_3_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5_1_3_0_2")) return false;
        instance_declaration_5_1_3_0_2_0(b, l + 1);
        return true;
    }

    // onls cidecls
    private static boolean instance_declaration_5_1_3_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instance_declaration_5_1_3_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && cidecls(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // ttype | q_name | LEFT_BRACKET q_name+ RIGHT_BRACKET | LEFT_PAREN q_name+ RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN
    public static boolean instvar(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instvar")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_INSTVAR, "<instvar>");
        r = ttype(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        if (!r) r = instvar_2(b, l + 1);
        if (!r) r = instvar_3(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // LEFT_BRACKET q_name+ RIGHT_BRACKET
    private static boolean instvar_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instvar_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_BRACKET);
        r = r && instvar_2_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean instvar_2_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instvar_2_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "instvar_2_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN q_name+ RIGHT_PAREN
    private static boolean instvar_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instvar_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && instvar_3_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean instvar_3_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "instvar_3_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "instvar_3_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // DATA | CLASS | INSTANCE | NEWTYPE | DERIVING | DEFAULT | TYPE_FAMILY | TYPE | TYPE_INSTANCE | IMPORT | MODULE
    static boolean keyword(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "keyword")) return false;
        boolean r;
        r = consumeToken(b, HS_DATA);
        if (!r) r = consumeToken(b, HS_CLASS);
        if (!r) r = consumeToken(b, HS_INSTANCE);
        if (!r) r = consumeToken(b, HS_NEWTYPE);
        if (!r) r = consumeToken(b, HS_DERIVING);
        if (!r) r = consumeToken(b, HS_DEFAULT);
        if (!r) r = consumeToken(b, HS_TYPE_FAMILY);
        if (!r) r = consumeToken(b, HS_TYPE);
        if (!r) r = consumeToken(b, HS_TYPE_INSTANCE);
        if (!r) r = consumeToken(b, HS_IMPORT);
        if (!r) r = consumeToken(b, HS_MODULE);
        return r;
    }

    /* ********************************************************** */
    // q_name COLON_COLON ttype
    public static boolean kind_signature(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "kind_signature")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_KIND_SIGNATURE, "<kind signature>");
        r = q_name(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && ttype(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // LET LEFT_BRACE let_definition* cdecl? RIGHT_BRACE? semi IN expression
    public static boolean let_abstraction(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "let_abstraction")) return false;
        if (!nextTokenIs(b, HS_LET)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_LET_ABSTRACTION, null);
        r = consumeTokens(b, 1, HS_LET, HS_LEFT_BRACE);
        p = r; // pin = 1
        r = r && report_error_(b, let_abstraction_2(b, l + 1));
        r = p && report_error_(b, let_abstraction_3(b, l + 1)) && r;
        r = p && report_error_(b, let_abstraction_4(b, l + 1)) && r;
        r = p && report_error_(b, consumeTokens(b, -1, HS_SEMI, HS_IN)) && r;
        r = p && expression(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // let_definition*
    private static boolean let_abstraction_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "let_abstraction_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!let_definition(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "let_abstraction_2", c)) break;
        }
        return true;
    }

    // cdecl?
    private static boolean let_abstraction_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "let_abstraction_3")) return false;
        cdecl(b, l + 1);
        return true;
    }

    // RIGHT_BRACE?
    private static boolean let_abstraction_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "let_abstraction_4")) return false;
        consumeToken(b, HS_RIGHT_BRACE);
        return true;
    }

    /* ********************************************************** */
    // cdecl SEMICOLON
    static boolean let_definition(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "let_definition")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = cdecl(b, l + 1);
        r = r && consumeToken(b, HS_SEMICOLON);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (general_id | LIST_COMPREHENSION)+ | NOT_TERMINATED_QQ_EXPRESSION
    static boolean line_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "line_expression")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = line_expression_0(b, l + 1);
        if (!r) r = consumeToken(b, HS_NOT_TERMINATED_QQ_EXPRESSION);
        exit_section_(b, m, null, r);
        return r;
    }

    // (general_id | LIST_COMPREHENSION)+
    private static boolean line_expression_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "line_expression_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = line_expression_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!line_expression_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "line_expression_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // general_id | LIST_COMPREHENSION
    private static boolean line_expression_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "line_expression_0_0")) return false;
        boolean r;
        r = general_id(b, l + 1);
        if (!r) r = consumeToken(b, HS_LIST_COMPREHENSION);
        return r;
    }

    /* ********************************************************** */
    // LEFT_BRACKET (COLON_COLON | q_name) RIGHT_BRACKET
    public static boolean list_type(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "list_type")) return false;
        if (!nextTokenIs(b, HS_LEFT_BRACKET)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_BRACKET);
        r = r && list_type_1(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_BRACKET);
        exit_section_(b, m, HS_LIST_TYPE, r);
        return r;
    }

    // COLON_COLON | q_name
    private static boolean list_type_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "list_type_1")) return false;
        boolean r;
        r = consumeToken(b, HS_COLON_COLON);
        if (!r) r = q_name(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // DECIMAL | HEXADECIMAL | OCTAL | FLOAT | CHARACTER_LITERAL | text_literal
    static boolean literal(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "literal")) return false;
        boolean r;
        r = consumeToken(b, HS_DECIMAL);
        if (!r) r = consumeToken(b, HS_HEXADECIMAL);
        if (!r) r = consumeToken(b, HS_OCTAL);
        if (!r) r = consumeToken(b, HS_FLOAT);
        if (!r) r = consumeToken(b, HS_CHARACTER_LITERAL);
        if (!r) r = text_literal(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // (conid DOT)* conid
    public static boolean modid(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "modid")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_MODID, "<modid>");
        r = modid_0(b, l + 1);
        p = r; // pin = 1
        r = r && conid(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // (conid DOT)*
    private static boolean modid_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "modid_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!modid_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "modid_0", c)) break;
        }
        return true;
    }

    // conid DOT
    private static boolean modid_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "modid_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // module_declaration onl body | onl body
    public static boolean module_body(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_body")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_MODULE_BODY, "<module body>");
        r = module_body_0(b, l + 1);
        if (!r) r = module_body_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // module_declaration onl body
    private static boolean module_body_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_body_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = module_declaration(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && body(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // onl body
    private static boolean module_body_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_body_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && body(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // MODULE modid onl pragma? onl (exports onl)? where_clause
    public static boolean module_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_declaration")) return false;
        if (!nextTokenIs(b, HS_MODULE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_MODULE);
        r = r && modid(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && module_declaration_3(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && module_declaration_5(b, l + 1);
        r = r && where_clause(b, l + 1);
        exit_section_(b, m, HS_MODULE_DECLARATION, r);
        return r;
    }

    // pragma?
    private static boolean module_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_declaration_3")) return false;
        pragma(b, l + 1);
        return true;
    }

    // (exports onl)?
    private static boolean module_declaration_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_declaration_5")) return false;
        module_declaration_5_0(b, l + 1);
        return true;
    }

    // exports onl
    private static boolean module_declaration_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "module_declaration_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = exports(b, l + 1);
        r = r && onl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // newconstr_fielddecl | q_name+ (onls ttype)?
    public static boolean newconstr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_NEWCONSTR, "<newconstr>");
        r = newconstr_fielddecl(b, l + 1);
        if (!r) r = newconstr_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_name+ (onls ttype)?
    private static boolean newconstr_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = newconstr_1_0(b, l + 1);
        r = r && newconstr_1_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean newconstr_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "newconstr_1_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls ttype)?
    private static boolean newconstr_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_1_1")) return false;
        newconstr_1_1_0(b, l + 1);
        return true;
    }

    // onls ttype
    private static boolean newconstr_1_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_1_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_name+ onl LEFT_PAREN onl ttype onl RIGHT_PAREN
    public static boolean newconstr_fielddecl(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_fielddecl")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_NEWCONSTR_FIELDDECL, "<newconstr fielddecl>");
        r = newconstr_fielddecl_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_name+
    private static boolean newconstr_fielddecl_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newconstr_fielddecl_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "newconstr_fielddecl_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // NEWTYPE onls INSTANCE? (onls pragma)? (onls ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype onls EQUAL onls newconstr (onl DERIVING (onl NEWTYPE)? onl atype)?
    public static boolean newtype_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration")) return false;
        if (!nextTokenIs(b, HS_NEWTYPE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_NEWTYPE);
        r = r && onls(b, l + 1);
        r = r && newtype_declaration_2(b, l + 1);
        r = r && newtype_declaration_3(b, l + 1);
        r = r && newtype_declaration_4(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_EQUAL);
        r = r && onls(b, l + 1);
        r = r && newconstr(b, l + 1);
        r = r && newtype_declaration_11(b, l + 1);
        exit_section_(b, m, HS_NEWTYPE_DECLARATION, r);
        return r;
    }

    // INSTANCE?
    private static boolean newtype_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_2")) return false;
        consumeToken(b, HS_INSTANCE);
        return true;
    }

    // (onls pragma)?
    private static boolean newtype_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_3")) return false;
        newtype_declaration_3_0(b, l + 1);
        return true;
    }

    // onls pragma
    private static boolean newtype_declaration_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls ccontext onls DOUBLE_RIGHT_ARROW)?
    private static boolean newtype_declaration_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_4")) return false;
        newtype_declaration_4_0(b, l + 1);
        return true;
    }

    // onls ccontext onls DOUBLE_RIGHT_ARROW
    private static boolean newtype_declaration_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && ccontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl DERIVING (onl NEWTYPE)? onl atype)?
    private static boolean newtype_declaration_11(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_11")) return false;
        newtype_declaration_11_0(b, l + 1);
        return true;
    }

    // onl DERIVING (onl NEWTYPE)? onl atype
    private static boolean newtype_declaration_11_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_11_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_DERIVING);
        r = r && newtype_declaration_11_0_2(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && atype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl NEWTYPE)?
    private static boolean newtype_declaration_11_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_11_0_2")) return false;
        newtype_declaration_11_0_2_0(b, l + 1);
        return true;
    }

    // onl NEWTYPE
    private static boolean newtype_declaration_11_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "newtype_declaration_11_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_NEWTYPE);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (&<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma)+
    static boolean nls(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!nls_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "nls", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma
    private static boolean nls_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls_0_0(b, l + 1);
        if (!r) r = nls_0_1(b, l + 1);
        if (!r) r = pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>> NEWLINE
    private static boolean nls_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls_0_0_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>>
    private static boolean nls_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = containsSpaces(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword
    private static boolean nls_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = nls_0_1_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        r = r && nls_0_1_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DIRECTIVE | INCLUDE_DIRECTIVE
    private static boolean nls_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0_1_0")) return false;
        boolean r;
        r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        return r;
    }

    // !keyword
    private static boolean nls_0_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "nls_0_1_2")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NOT_);
        r = !keyword(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // (DIRECTIVE | NEWLINE | INCLUDE_DIRECTIVE | pragma)*
    static boolean onl(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onl")) return false;
        while (true) {
            int c = current_position_(b);
            if (!onl_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "onl", c)) break;
        }
        return true;
    }

    // DIRECTIVE | NEWLINE | INCLUDE_DIRECTIVE | pragma
    private static boolean onl_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onl_0")) return false;
        boolean r;
        r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_NEWLINE);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        if (!r) r = pragma(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // (&<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma)*
    static boolean onls(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls")) return false;
        while (true) {
            int c = current_position_(b);
            if (!onls_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "onls", c)) break;
        }
        return true;
    }

    // &<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma
    private static boolean onls_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls_0_0(b, l + 1);
        if (!r) r = onls_0_1(b, l + 1);
        if (!r) r = pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>> NEWLINE
    private static boolean onls_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls_0_0_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>>
    private static boolean onls_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = containsSpaces(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword
    private static boolean onls_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls_0_1_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        r = r && onls_0_1_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DIRECTIVE | INCLUDE_DIRECTIVE
    private static boolean onls_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0_1_0")) return false;
        boolean r;
        r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        return r;
    }

    // !keyword
    private static boolean onls_0_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "onls_0_1_2")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NOT_);
        r = !keyword(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // (&<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma)?
    static boolean oonls(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls")) return false;
        oonls_0(b, l + 1);
        return true;
    }

    // &<<containsSpaces>> NEWLINE | (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword | pragma
    private static boolean oonls_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls_0_0(b, l + 1);
        if (!r) r = oonls_0_1(b, l + 1);
        if (!r) r = pragma(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>> NEWLINE
    private static boolean oonls_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls_0_0_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<containsSpaces>>
    private static boolean oonls_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = containsSpaces(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (DIRECTIVE | INCLUDE_DIRECTIVE) NEWLINE !keyword
    private static boolean oonls_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls_0_1_0(b, l + 1);
        r = r && consumeToken(b, HS_NEWLINE);
        r = r && oonls_0_1_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DIRECTIVE | INCLUDE_DIRECTIVE
    private static boolean oonls_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0_1_0")) return false;
        boolean r;
        r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = consumeToken(b, HS_INCLUDE_DIRECTIVE);
        return r;
    }

    // !keyword
    private static boolean oonls_0_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "oonls_0_1_2")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NOT_);
        r = !keyword(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // LEFT_PAREN expression RIGHT_PAREN
    public static boolean paren_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "paren_expression")) return false;
        if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && expression(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, HS_PAREN_EXPRESSION, r);
        return r;
    }

    /* ********************************************************** */
    // PRAGMA_START general_pragma_content* PRAGMA_END NEWLINE?
    public static boolean pragma(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "pragma")) return false;
        if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_PRAGMA, null);
        r = consumeToken(b, HS_PRAGMA_START);
        p = r; // pin = 1
        r = r && report_error_(b, pragma_1(b, l + 1));
        r = p && report_error_(b, consumeToken(b, HS_PRAGMA_END)) && r;
        r = p && pragma_3(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // general_pragma_content*
    private static boolean pragma_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "pragma_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!general_pragma_content(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "pragma_1", c)) break;
        }
        return true;
    }

    // NEWLINE?
    private static boolean pragma_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "pragma_3")) return false;
        consumeToken(b, HS_NEWLINE);
        return true;
    }

    /* ********************************************************** */
    // NEWLINE* shebang_line? NEWLINE* file_header NEWLINE* module_body?
    static boolean program(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = program_0(b, l + 1);
        r = r && program_1(b, l + 1);
        r = r && program_2(b, l + 1);
        r = r && file_header(b, l + 1);
        r = r && program_4(b, l + 1);
        r = r && program_5(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // NEWLINE*
    private static boolean program_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_NEWLINE)) break;
            if (!empty_element_parsed_guard_(b, "program_0", c)) break;
        }
        return true;
    }

    // shebang_line?
    private static boolean program_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program_1")) return false;
        shebang_line(b, l + 1);
        return true;
    }

    // NEWLINE*
    private static boolean program_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_NEWLINE)) break;
            if (!empty_element_parsed_guard_(b, "program_2", c)) break;
        }
        return true;
    }

    // NEWLINE*
    private static boolean program_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_NEWLINE)) break;
            if (!empty_element_parsed_guard_(b, "program_4", c)) break;
        }
        return true;
    }

    // module_body?
    private static boolean program_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "program_5")) return false;
        module_body(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // &<<noSpaceAfterQualifier>> q_con_qualifier4 DOT conid | &<<noSpaceAfterQualifier>> q_con_qualifier3 DOT conid | &<<noSpaceAfterQualifier>> q_con_qualifier2 DOT conid | &<<noSpaceAfterQualifier>> q_con_qualifier1 DOT conid
    public static boolean q_con(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_Q_CON, "<q con>");
        r = q_con_0(b, l + 1);
        if (!r) r = q_con_1(b, l + 1);
        if (!r) r = q_con_2(b, l + 1);
        if (!r) r = q_con_3(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // &<<noSpaceAfterQualifier>> q_con_qualifier4 DOT conid
    private static boolean q_con_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_con_0_0(b, l + 1);
        r = r && q_con_qualifier4(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<noSpaceAfterQualifier>>
    private static boolean q_con_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = noSpaceAfterQualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // &<<noSpaceAfterQualifier>> q_con_qualifier3 DOT conid
    private static boolean q_con_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_con_1_0(b, l + 1);
        r = r && q_con_qualifier3(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<noSpaceAfterQualifier>>
    private static boolean q_con_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = noSpaceAfterQualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // &<<noSpaceAfterQualifier>> q_con_qualifier2 DOT conid
    private static boolean q_con_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_con_2_0(b, l + 1);
        r = r && q_con_qualifier2(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<noSpaceAfterQualifier>>
    private static boolean q_con_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = noSpaceAfterQualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // &<<noSpaceAfterQualifier>> q_con_qualifier1 DOT conid
    private static boolean q_con_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_con_3_0(b, l + 1);
        r = r && q_con_qualifier1(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<noSpaceAfterQualifier>>
    private static boolean q_con_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = noSpaceAfterQualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // q_con_qualifier4 | q_con_qualifier3 | q_con_qualifier2 | q_con_qualifier1
    public static boolean q_con_qualifier(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_qualifier")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_con_qualifier4(b, l + 1);
        if (!r) r = q_con_qualifier3(b, l + 1);
        if (!r) r = q_con_qualifier2(b, l + 1);
        if (!r) r = q_con_qualifier1(b, l + 1);
        exit_section_(b, m, HS_Q_CON_QUALIFIER, r);
        return r;
    }

    /* ********************************************************** */
    // conid
    public static boolean q_con_qualifier1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_qualifier1")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        exit_section_(b, m, HS_Q_CON_QUALIFIER_1, r);
        return r;
    }

    /* ********************************************************** */
    // conid DOT conid
    public static boolean q_con_qualifier2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_qualifier2")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, HS_Q_CON_QUALIFIER_2, r);
        return r;
    }

    /* ********************************************************** */
    // conid DOT conid DOT conid
    public static boolean q_con_qualifier3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_qualifier3")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, HS_Q_CON_QUALIFIER_3, r);
        return r;
    }

    /* ********************************************************** */
    // conid DOT conid DOT conid DOT conid
    public static boolean q_con_qualifier4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_con_qualifier4")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, HS_Q_CON_QUALIFIER_4, r);
        return r;
    }

    /* ********************************************************** */
    // q_var_con | var_con | LEFT_PAREN q_var_con RIGHT_PAREN | LEFT_PAREN var_con RIGHT_PAREN | BACKQUOTE q_var_con BACKQUOTE | BACKQUOTE var_con BACKQUOTE
    public static boolean q_name(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_name")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_Q_NAME, "<q name>");
        r = q_var_con(b, l + 1);
        if (!r) r = var_con(b, l + 1);
        if (!r) r = q_name_2(b, l + 1);
        if (!r) r = q_name_3(b, l + 1);
        if (!r) r = q_name_4(b, l + 1);
        if (!r) r = q_name_5(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // LEFT_PAREN q_var_con RIGHT_PAREN
    private static boolean q_name_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_name_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && q_var_con(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN var_con RIGHT_PAREN
    private static boolean q_name_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_name_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && var_con(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // BACKQUOTE q_var_con BACKQUOTE
    private static boolean q_name_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_name_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_BACKQUOTE);
        r = r && q_var_con(b, l + 1);
        r = r && consumeToken(b, HS_BACKQUOTE);
        exit_section_(b, m, null, r);
        return r;
    }

    // BACKQUOTE var_con BACKQUOTE
    private static boolean q_name_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_name_5")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_BACKQUOTE);
        r = r && var_con(b, l + 1);
        r = r && consumeToken(b, HS_BACKQUOTE);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_name (COMMA q_name)*
    public static boolean q_names(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_names")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_Q_NAMES, "<q names>");
        r = q_name(b, l + 1);
        r = r && q_names_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (COMMA q_name)*
    private static boolean q_names_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_names_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_names_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "q_names_1", c)) break;
        }
        return true;
    }

    // COMMA q_name
    private static boolean q_names_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_names_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COMMA);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // &<<noSpaceAfterQualifier>> qualifier DOT (varid | consym | varsym) | q_con
    public static boolean q_var_con(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_var_con")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_Q_VAR_CON, "<q var con>");
        r = q_var_con_0(b, l + 1);
        if (!r) r = q_con(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // &<<noSpaceAfterQualifier>> qualifier DOT (varid | consym | varsym)
    private static boolean q_var_con_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_var_con_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_var_con_0_0(b, l + 1);
        r = r && qualifier(b, l + 1);
        r = r && consumeToken(b, HS_DOT);
        r = r && q_var_con_0_3(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // &<<noSpaceAfterQualifier>>
    private static boolean q_var_con_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_var_con_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _AND_);
        r = noSpaceAfterQualifier(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // varid | consym | varsym
    private static boolean q_var_con_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "q_var_con_0_3")) return false;
        boolean r;
        r = varid(b, l + 1);
        if (!r) r = consym(b, l + 1);
        if (!r) r = varsym(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // conid (DOT conid)*
    public static boolean qualifier(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "qualifier")) return false;
        if (!nextTokenIs(b, HS_CON_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conid(b, l + 1);
        r = r && qualifier_1(b, l + 1);
        exit_section_(b, m, HS_QUALIFIER, r);
        return r;
    }

    // (DOT conid)*
    private static boolean qualifier_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "qualifier_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!qualifier_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "qualifier_1", c)) break;
        }
        return true;
    }

    // DOT conid
    private static boolean qualifier_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "qualifier_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        r = r && conid(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // QUASIQUOTE
    public static boolean quasi_quote(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "quasi_quote")) return false;
        if (!nextTokenIs(b, HS_QUASIQUOTE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_QUASIQUOTE);
        exit_section_(b, m, HS_QUASI_QUOTE, r);
        return r;
    }

    /* ********************************************************** */
    // CASE | CLASS | DATA | DEFAULT | DERIVING | DO | ELSE | IF | IMPORT | IN | INSTANCE | LET | MODULE | NEWTYPE | OF | THEN | TYPE | WHERE | UNDERSCORE
    static boolean reserved_id(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "reserved_id")) return false;
        boolean r;
        r = consumeToken(b, HS_CASE);
        if (!r) r = consumeToken(b, HS_CLASS);
        if (!r) r = consumeToken(b, HS_DATA);
        if (!r) r = consumeToken(b, HS_DEFAULT);
        if (!r) r = consumeToken(b, HS_DERIVING);
        if (!r) r = consumeToken(b, HS_DO);
        if (!r) r = consumeToken(b, HS_ELSE);
        if (!r) r = consumeToken(b, HS_IF);
        if (!r) r = consumeToken(b, HS_IMPORT);
        if (!r) r = consumeToken(b, HS_IN);
        if (!r) r = consumeToken(b, HS_INSTANCE);
        if (!r) r = consumeToken(b, HS_LET);
        if (!r) r = consumeToken(b, HS_MODULE);
        if (!r) r = consumeToken(b, HS_NEWTYPE);
        if (!r) r = consumeToken(b, HS_OF);
        if (!r) r = consumeToken(b, HS_THEN);
        if (!r) r = consumeToken(b, HS_TYPE);
        if (!r) r = consumeToken(b, HS_WHERE);
        if (!r) r = consumeToken(b, HS_UNDERSCORE);
        return r;
    }

    /* ********************************************************** */
    // simpleclass | simpleclass onls LEFT_PAREN onls simpleclass (onls COMMA onls simpleclass)* onls RIGHT_PAREN
    public static boolean scontext(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scontext")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_SCONTEXT, "<scontext>");
        r = simpleclass(b, l + 1);
        if (!r) r = scontext_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // simpleclass onls LEFT_PAREN onls simpleclass (onls COMMA onls simpleclass)* onls RIGHT_PAREN
    private static boolean scontext_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scontext_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = simpleclass(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && simpleclass(b, l + 1);
        r = r && scontext_1_5(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls COMMA onls simpleclass)*
    private static boolean scontext_1_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scontext_1_5")) return false;
        while (true) {
            int c = current_position_(b);
            if (!scontext_1_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "scontext_1_5", c)) break;
        }
        return true;
    }

    // onls COMMA onls simpleclass
    private static boolean scontext_1_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scontext_1_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && simpleclass(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // "#!" general_id+
    public static boolean shebang_line(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "shebang_line")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_SHEBANG_LINE, "<shebang line>");
        r = consumeToken(b, "#!");
        r = r && shebang_line_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // general_id+
    private static boolean shebang_line_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "shebang_line_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = general_id(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!general_id(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "shebang_line_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // ttype | q_name+ LEFT_PAREN q_name+ RIGHT_PAREN q_name* | q_name+
    public static boolean simpleclass(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_SIMPLECLASS, "<simpleclass>");
        r = ttype(b, l + 1);
        if (!r) r = simpleclass_1(b, l + 1);
        if (!r) r = simpleclass_2(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_name+ LEFT_PAREN q_name+ RIGHT_PAREN q_name*
    private static boolean simpleclass_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = simpleclass_1_0(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && simpleclass_1_2(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        r = r && simpleclass_1_4(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean simpleclass_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleclass_1_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean simpleclass_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass_1_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleclass_1_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean simpleclass_1_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass_1_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleclass_1_4", c)) break;
        }
        return true;
    }

    // q_name+
    private static boolean simpleclass_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleclass_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleclass_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // ttype | (q_name | LEFT_PAREN RIGHT_PAREN)+ |
    //                                   q_name* oonls LEFT_PAREN q_name RIGHT_PAREN oonls q_name* |
    //                                   q_name oonls q_name* oonls (LEFT_PAREN type_signature RIGHT_PAREN)+ oonls q_name* |
    //                                   q_name | LEFT_PAREN RIGHT_PAREN | LEFT_BRACKET RIGHT_BRACKET q_name*
    public static boolean simpletype(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_SIMPLETYPE, "<simpletype>");
        r = ttype(b, l + 1);
        if (!r) r = simpletype_1(b, l + 1);
        if (!r) r = simpletype_2(b, l + 1);
        if (!r) r = simpletype_3(b, l + 1);
        if (!r) r = q_name(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        if (!r) r = simpletype_6(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (q_name | LEFT_PAREN RIGHT_PAREN)+
    private static boolean simpletype_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = simpletype_1_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!simpletype_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_1", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name | LEFT_PAREN RIGHT_PAREN
    private static boolean simpletype_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name* oonls LEFT_PAREN q_name RIGHT_PAREN oonls q_name*
    private static boolean simpletype_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = simpletype_2_0(b, l + 1);
        r = r && oonls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && q_name(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        r = r && oonls(b, l + 1);
        r = r && simpletype_2_6(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean simpletype_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_2_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_2_0", c)) break;
        }
        return true;
    }

    // q_name*
    private static boolean simpletype_2_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_2_6")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_2_6", c)) break;
        }
        return true;
    }

    // q_name oonls q_name* oonls (LEFT_PAREN type_signature RIGHT_PAREN)+ oonls q_name*
    private static boolean simpletype_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        r = r && oonls(b, l + 1);
        r = r && simpletype_3_2(b, l + 1);
        r = r && oonls(b, l + 1);
        r = r && simpletype_3_4(b, l + 1);
        r = r && oonls(b, l + 1);
        r = r && simpletype_3_6(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean simpletype_3_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_3_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_3_2", c)) break;
        }
        return true;
    }

    // (LEFT_PAREN type_signature RIGHT_PAREN)+
    private static boolean simpletype_3_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_3_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = simpletype_3_4_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!simpletype_3_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_3_4", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN type_signature RIGHT_PAREN
    private static boolean simpletype_3_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_3_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && type_signature(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean simpletype_3_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_3_6")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_3_6", c)) break;
        }
        return true;
    }

    // LEFT_BRACKET RIGHT_BRACKET q_name*
    private static boolean simpletype_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_LEFT_BRACKET, HS_RIGHT_BRACKET);
        r = r && simpletype_6_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean simpletype_6_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpletype_6_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpletype_6_2", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // dot_dot | COLON_COLON | EQUAL | BACKSLASH | VERTICAL_BAR | LEFT_ARROW | RIGHT_ARROW | AT | TILDE | DOUBLE_RIGHT_ARROW
    static boolean symbol_reserved_op(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "symbol_reserved_op")) return false;
        boolean r;
        r = dot_dot(b, l + 1);
        if (!r) r = consumeToken(b, HS_COLON_COLON);
        if (!r) r = consumeToken(b, HS_EQUAL);
        if (!r) r = consumeToken(b, HS_BACKSLASH);
        if (!r) r = consumeToken(b, HS_VERTICAL_BAR);
        if (!r) r = consumeToken(b, HS_LEFT_ARROW);
        if (!r) r = consumeToken(b, HS_RIGHT_ARROW);
        if (!r) r = consumeToken(b, HS_AT);
        if (!r) r = consumeToken(b, HS_TILDE);
        if (!r) r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        return r;
    }

    /* ********************************************************** */
    // STRING_LITERAL
    public static boolean text_literal(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "text_literal")) return false;
        if (!nextTokenIs(b, HS_STRING_LITERAL)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_STRING_LITERAL);
        exit_section_(b, m, HS_TEXT_LITERAL, r);
        return r;
    }

    /* ********************************************************** */
    // (type_declaration | data_declaration | newtype_declaration | class_declaration | instance_declaration | default_declaration |
    //                                   foreign_declaration | type_family_declaration | deriving_declaration | type_instance_declaration | type_signature |
    //                                   pragma | fixity_declaration | expression | DIRECTIVE | implementation_declaration) SEMICOLON? NEWLINE?
    public static boolean top_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declaration")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, HS_TOP_DECLARATION, "<top declaration>");
        r = top_declaration_0(b, l + 1);
        r = r && top_declaration_1(b, l + 1);
        r = r && top_declaration_2(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // type_declaration | data_declaration | newtype_declaration | class_declaration | instance_declaration | default_declaration |
    //                                   foreign_declaration | type_family_declaration | deriving_declaration | type_instance_declaration | type_signature |
    //                                   pragma | fixity_declaration | expression | DIRECTIVE | implementation_declaration
    private static boolean top_declaration_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declaration_0")) return false;
        boolean r;
        r = type_declaration(b, l + 1);
        if (!r) r = data_declaration(b, l + 1);
        if (!r) r = newtype_declaration(b, l + 1);
        if (!r) r = class_declaration(b, l + 1);
        if (!r) r = instance_declaration(b, l + 1);
        if (!r) r = default_declaration(b, l + 1);
        if (!r) r = foreign_declaration(b, l + 1);
        if (!r) r = type_family_declaration(b, l + 1);
        if (!r) r = deriving_declaration(b, l + 1);
        if (!r) r = type_instance_declaration(b, l + 1);
        if (!r) r = type_signature(b, l + 1);
        if (!r) r = pragma(b, l + 1);
        if (!r) r = fixity_declaration(b, l + 1);
        if (!r) r = expression(b, l + 1);
        if (!r) r = consumeToken(b, HS_DIRECTIVE);
        if (!r) r = implementation_declaration(b, l + 1);
        return r;
    }

    // SEMICOLON?
    private static boolean top_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declaration_1")) return false;
        consumeToken(b, HS_SEMICOLON);
        return true;
    }

    // NEWLINE?
    private static boolean top_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declaration_2")) return false;
        consumeToken(b, HS_NEWLINE);
        return true;
    }

    /* ********************************************************** */
    // (top_declaration onl)* top_declaration?
    static boolean top_declarations(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declarations")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = top_declarations_0(b, l + 1);
        r = r && top_declarations_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (top_declaration onl)*
    private static boolean top_declarations_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declarations_0")) return false;
        while (true) {
            int c = current_position_(b);
            if (!top_declarations_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "top_declarations_0", c)) break;
        }
        return true;
    }

    // top_declaration onl
    private static boolean top_declarations_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declarations_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = top_declaration(b, l + 1);
        r = r && onl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // top_declaration?
    private static boolean top_declarations_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "top_declarations_1")) return false;
        top_declaration(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // "!"? (btype (oonls RIGHT_ARROW oonls ttype)* | list_type q_name* | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN)
    public static boolean ttype(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, HS_TTYPE, "<ttype>");
        r = ttype_0(b, l + 1);
        r = r && ttype_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // "!"?
    private static boolean ttype_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_0")) return false;
        consumeToken(b, "!");
        return true;
    }

    // btype (oonls RIGHT_ARROW oonls ttype)* | list_type q_name* | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN
    private static boolean ttype_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ttype_1_0(b, l + 1);
        if (!r) r = ttype_1_1(b, l + 1);
        if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_ARROW, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // btype (oonls RIGHT_ARROW oonls ttype)*
    private static boolean ttype_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = btype(b, l + 1);
        r = r && ttype_1_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (oonls RIGHT_ARROW oonls ttype)*
    private static boolean ttype_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1_0_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!ttype_1_0_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "ttype_1_0_1", c)) break;
        }
        return true;
    }

    // oonls RIGHT_ARROW oonls ttype
    private static boolean ttype_1_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = oonls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_ARROW);
        r = r && oonls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // list_type q_name*
    private static boolean ttype_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = list_type(b, l + 1);
        r = r && ttype_1_1_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name*
    private static boolean ttype_1_1_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype_1_1_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "ttype_1_1_1", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // (onl "#")? onl ttype ((onl COMMA)? onl ttype)* onl ("#" onl)?
    static boolean ttype1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ttype1_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && ttype1_3(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && ttype1_5(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl "#")?
    private static boolean ttype1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_0")) return false;
        ttype1_0_0(b, l + 1);
        return true;
    }

    // onl "#"
    private static boolean ttype1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, "#");
        exit_section_(b, m, null, r);
        return r;
    }

    // ((onl COMMA)? onl ttype)*
    private static boolean ttype1_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!ttype1_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "ttype1_3", c)) break;
        }
        return true;
    }

    // (onl COMMA)? onl ttype
    private static boolean ttype1_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ttype1_3_0_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onl COMMA)?
    private static boolean ttype1_3_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_3_0_0")) return false;
        ttype1_3_0_0_0(b, l + 1);
        return true;
    }

    // onl COMMA
    private static boolean ttype1_3_0_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_3_0_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        exit_section_(b, m, null, r);
        return r;
    }

    // ("#" onl)?
    private static boolean ttype1_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_5")) return false;
        ttype1_5_0(b, l + 1);
        return true;
    }

    // "#" onl
    private static boolean ttype1_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype1_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, "#");
        r = r && onl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // ("forall" | FORALL)? onl ttype (onl DOUBLE_RIGHT_ARROW onl ttype)? onl
    static boolean ttype2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = ttype2_0(b, l + 1);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && ttype2_3(b, l + 1);
        r = r && onl(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // ("forall" | FORALL)?
    private static boolean ttype2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype2_0")) return false;
        ttype2_0_0(b, l + 1);
        return true;
    }

    // "forall" | FORALL
    private static boolean ttype2_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype2_0_0")) return false;
        boolean r;
        r = consumeToken(b, "forall");
        if (!r) r = consumeToken(b, HS_FORALL);
        return r;
    }

    // (onl DOUBLE_RIGHT_ARROW onl ttype)?
    private static boolean ttype2_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype2_3")) return false;
        ttype2_3_0(b, l + 1);
        return true;
    }

    // onl DOUBLE_RIGHT_ARROW onl ttype
    private static boolean ttype2_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "ttype2_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onl(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        r = r && onl(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // TYPE onls simpletype onls COLON_COLON onls ttype |
    //                                   TYPE onls simpletype onls EQUAL onls ttype onls VERTICAL_BAR q_name RIGHT_ARROW q_name |
    //                                   TYPE onls simpletype onls (EQUAL | WHERE) onls (ttype | type_signature) (DOUBLE_RIGHT_ARROW ttype)? |
    //                                   TYPE onls simpletype onls EQUAL onls expression |
    //                                   TYPE onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls (EQUAL onls ttype | COLON_COLON ttype) |
    //                                   TYPE onls simpletype
    public static boolean type_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration")) return false;
        if (!nextTokenIs(b, HS_TYPE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_declaration_0(b, l + 1);
        if (!r) r = type_declaration_1(b, l + 1);
        if (!r) r = type_declaration_2(b, l + 1);
        if (!r) r = type_declaration_3(b, l + 1);
        if (!r) r = type_declaration_4(b, l + 1);
        if (!r) r = type_declaration_5(b, l + 1);
        exit_section_(b, m, HS_TYPE_DECLARATION, r);
        return r;
    }

    // TYPE onls simpletype onls COLON_COLON onls ttype
    private static boolean type_declaration_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // TYPE onls simpletype onls EQUAL onls ttype onls VERTICAL_BAR q_name RIGHT_ARROW q_name
    private static boolean type_declaration_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_EQUAL);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_VERTICAL_BAR);
        r = r && q_name(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_ARROW);
        r = r && q_name(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // TYPE onls simpletype onls (EQUAL | WHERE) onls (ttype | type_signature) (DOUBLE_RIGHT_ARROW ttype)?
    private static boolean type_declaration_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && type_declaration_2_4(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && type_declaration_2_6(b, l + 1);
        r = r && type_declaration_2_7(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // EQUAL | WHERE
    private static boolean type_declaration_2_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_2_4")) return false;
        boolean r;
        r = consumeToken(b, HS_EQUAL);
        if (!r) r = consumeToken(b, HS_WHERE);
        return r;
    }

    // ttype | type_signature
    private static boolean type_declaration_2_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_2_6")) return false;
        boolean r;
        r = ttype(b, l + 1);
        if (!r) r = type_signature(b, l + 1);
        return r;
    }

    // (DOUBLE_RIGHT_ARROW ttype)?
    private static boolean type_declaration_2_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_2_7")) return false;
        type_declaration_2_7_0(b, l + 1);
        return true;
    }

    // DOUBLE_RIGHT_ARROW ttype
    private static boolean type_declaration_2_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_2_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // TYPE onls simpletype onls EQUAL onls expression
    private static boolean type_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_EQUAL);
        r = r && onls(b, l + 1);
        r = r && expression(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // TYPE onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls (EQUAL onls ttype | COLON_COLON ttype)
    private static boolean type_declaration_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && type_declaration_4_4(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && type_declaration_4_6(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
    private static boolean type_declaration_4_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4_4")) return false;
        while (true) {
            int c = current_position_(b);
            if (!type_declaration_4_4_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "type_declaration_4_4", c)) break;
        }
        return true;
    }

    // LEFT_PAREN onls kind_signature onls RIGHT_PAREN
    private static boolean type_declaration_4_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && kind_signature(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    // EQUAL onls ttype | COLON_COLON ttype
    private static boolean type_declaration_4_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_declaration_4_6_0(b, l + 1);
        if (!r) r = type_declaration_4_6_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // EQUAL onls ttype
    private static boolean type_declaration_4_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_EQUAL);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // COLON_COLON ttype
    private static boolean type_declaration_4_6_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_4_6_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COLON_COLON);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // TYPE onls simpletype
    private static boolean type_declaration_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_declaration_5")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE);
        r = r && onls(b, l + 1);
        r = r && simpletype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // q_name+ TILDE q_name+
    public static boolean type_equality(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_equality")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_TYPE_EQUALITY, "<type equality>");
        r = type_equality_0(b, l + 1);
        r = r && consumeToken(b, HS_TILDE);
        r = r && type_equality_2(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_name+
    private static boolean type_equality_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_equality_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "type_equality_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name+
    private static boolean type_equality_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_equality_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!q_name(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "type_equality_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // TYPE_FAMILY onls type_family_type (onls (WHERE | EQUAL) (onls expression)?)?
    public static boolean type_family_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration")) return false;
        if (!nextTokenIs(b, HS_TYPE_FAMILY)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE_FAMILY);
        r = r && onls(b, l + 1);
        r = r && type_family_type(b, l + 1);
        r = r && type_family_declaration_3(b, l + 1);
        exit_section_(b, m, HS_TYPE_FAMILY_DECLARATION, r);
        return r;
    }

    // (onls (WHERE | EQUAL) (onls expression)?)?
    private static boolean type_family_declaration_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration_3")) return false;
        type_family_declaration_3_0(b, l + 1);
        return true;
    }

    // onls (WHERE | EQUAL) (onls expression)?
    private static boolean type_family_declaration_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && type_family_declaration_3_0_1(b, l + 1);
        r = r && type_family_declaration_3_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // WHERE | EQUAL
    private static boolean type_family_declaration_3_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration_3_0_1")) return false;
        boolean r;
        r = consumeToken(b, HS_WHERE);
        if (!r) r = consumeToken(b, HS_EQUAL);
        return r;
    }

    // (onls expression)?
    private static boolean type_family_declaration_3_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration_3_0_2")) return false;
        type_family_declaration_3_0_2_0(b, l + 1);
        return true;
    }

    // onls expression
    private static boolean type_family_declaration_3_0_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_declaration_3_0_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && expression(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (q_name | LEFT_PAREN? q_names COLON_COLON? (onls ccontext DOUBLE_RIGHT_ARROW)? (onls ttype)? RIGHT_PAREN?)+ (COLON_COLON onls ttype)?
    public static boolean type_family_type(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_TYPE_FAMILY_TYPE, "<type family type>");
        r = type_family_type_0(b, l + 1);
        r = r && type_family_type_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // (q_name | LEFT_PAREN? q_names COLON_COLON? (onls ccontext DOUBLE_RIGHT_ARROW)? (onls ttype)? RIGHT_PAREN?)+
    private static boolean type_family_type_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_family_type_0_0(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!type_family_type_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "type_family_type_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // q_name | LEFT_PAREN? q_names COLON_COLON? (onls ccontext DOUBLE_RIGHT_ARROW)? (onls ttype)? RIGHT_PAREN?
    private static boolean type_family_type_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_name(b, l + 1);
        if (!r) r = type_family_type_0_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN? q_names COLON_COLON? (onls ccontext DOUBLE_RIGHT_ARROW)? (onls ttype)? RIGHT_PAREN?
    private static boolean type_family_type_0_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = type_family_type_0_0_1_0(b, l + 1);
        r = r && q_names(b, l + 1);
        r = r && type_family_type_0_0_1_2(b, l + 1);
        r = r && type_family_type_0_0_1_3(b, l + 1);
        r = r && type_family_type_0_0_1_4(b, l + 1);
        r = r && type_family_type_0_0_1_5(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // LEFT_PAREN?
    private static boolean type_family_type_0_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_0")) return false;
        consumeToken(b, HS_LEFT_PAREN);
        return true;
    }

    // COLON_COLON?
    private static boolean type_family_type_0_0_1_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_2")) return false;
        consumeToken(b, HS_COLON_COLON);
        return true;
    }

    // (onls ccontext DOUBLE_RIGHT_ARROW)?
    private static boolean type_family_type_0_0_1_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_3")) return false;
        type_family_type_0_0_1_3_0(b, l + 1);
        return true;
    }

    // onls ccontext DOUBLE_RIGHT_ARROW
    private static boolean type_family_type_0_0_1_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && ccontext(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls ttype)?
    private static boolean type_family_type_0_0_1_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_4")) return false;
        type_family_type_0_0_1_4_0(b, l + 1);
        return true;
    }

    // onls ttype
    private static boolean type_family_type_0_0_1_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // RIGHT_PAREN?
    private static boolean type_family_type_0_0_1_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_0_0_1_5")) return false;
        consumeToken(b, HS_RIGHT_PAREN);
        return true;
    }

    // (COLON_COLON onls ttype)?
    private static boolean type_family_type_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_1")) return false;
        type_family_type_1_0(b, l + 1);
        return true;
    }

    // COLON_COLON onls ttype
    private static boolean type_family_type_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_family_type_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_COLON_COLON);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // TYPE_INSTANCE onls expression
    public static boolean type_instance_declaration(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_instance_declaration")) return false;
        if (!nextTokenIs(b, HS_TYPE_INSTANCE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_TYPE_INSTANCE);
        r = r && onls(b, l + 1);
        r = r && expression(b, l + 1);
        exit_section_(b, m, HS_TYPE_INSTANCE_DECLARATION, r);
        return r;
    }

    /* ********************************************************** */
    // q_names onls COLON_COLON (onls ccontext onls DOUBLE_RIGHT_ARROW)* onls ttype !EQUAL |
    //                                   q_names onls LEFT_PAREN onls q_names onls COMMA onls ccontext onls DOUBLE_RIGHT_ARROW onls ttype onls RIGHT_PAREN
    public static boolean type_signature(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_TYPE_SIGNATURE, "<type signature>");
        r = type_signature_0(b, l + 1);
        if (!r) r = type_signature_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_names onls COLON_COLON (onls ccontext onls DOUBLE_RIGHT_ARROW)* onls ttype !EQUAL
    private static boolean type_signature_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_names(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_COLON_COLON);
        r = r && type_signature_0_3(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && type_signature_0_6(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (onls ccontext onls DOUBLE_RIGHT_ARROW)*
    private static boolean type_signature_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature_0_3")) return false;
        while (true) {
            int c = current_position_(b);
            if (!type_signature_0_3_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "type_signature_0_3", c)) break;
        }
        return true;
    }

    // onls ccontext onls DOUBLE_RIGHT_ARROW
    private static boolean type_signature_0_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature_0_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = onls(b, l + 1);
        r = r && ccontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        exit_section_(b, m, null, r);
        return r;
    }

    // !EQUAL
    private static boolean type_signature_0_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature_0_6")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NOT_);
        r = !consumeToken(b, HS_EQUAL);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // q_names onls LEFT_PAREN onls q_names onls COMMA onls ccontext onls DOUBLE_RIGHT_ARROW onls ttype onls RIGHT_PAREN
    private static boolean type_signature_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "type_signature_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = q_names(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_LEFT_PAREN);
        r = r && onls(b, l + 1);
        r = r && q_names(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_COMMA);
        r = r && onls(b, l + 1);
        r = r && ccontext(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
        r = r && onls(b, l + 1);
        r = r && ttype(b, l + 1);
        r = r && onls(b, l + 1);
        r = r && consumeToken(b, HS_RIGHT_PAREN);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // varid | consym | varsym | conid
    public static boolean var_con(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "var_con")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_VAR_CON, "<var con>");
        r = varid(b, l + 1);
        if (!r) r = consym(b, l + 1);
        if (!r) r = varsym(b, l + 1);
        if (!r) r = conid(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // VAR_ID
    public static boolean varid(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varid")) return false;
        if (!nextTokenIs(b, HS_VAR_ID)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_VAR_ID);
        exit_section_(b, m, HS_VARID, r);
        return r;
    }

    /* ********************************************************** */
    // VARSYM_ID | DOT+ VARSYM_ID | DOT+ EQUAL | DOT+ BACKSLASH | DOT+ TILDE | DOT+ AT | DOT+ CONSYM_ID | DOT+ VERTICAL_BAR | DOT DOT DOT+ | DOT
    public static boolean varsym(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym")) return false;
        if (!nextTokenIs(b, "<varsym>", HS_DOT, HS_VARSYM_ID)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, HS_VARSYM, "<varsym>");
        r = consumeToken(b, HS_VARSYM_ID);
        if (!r) r = varsym_1(b, l + 1);
        if (!r) r = varsym_2(b, l + 1);
        if (!r) r = varsym_3(b, l + 1);
        if (!r) r = varsym_4(b, l + 1);
        if (!r) r = varsym_5(b, l + 1);
        if (!r) r = varsym_6(b, l + 1);
        if (!r) r = varsym_7(b, l + 1);
        if (!r) r = varsym_8(b, l + 1);
        if (!r) r = consumeToken(b, HS_DOT);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // DOT+ VARSYM_ID
    private static boolean varsym_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_1_0(b, l + 1);
        r = r && consumeToken(b, HS_VARSYM_ID);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_1_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ EQUAL
    private static boolean varsym_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_2_0(b, l + 1);
        r = r && consumeToken(b, HS_EQUAL);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_2_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ BACKSLASH
    private static boolean varsym_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_3")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_3_0(b, l + 1);
        r = r && consumeToken(b, HS_BACKSLASH);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_3_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ TILDE
    private static boolean varsym_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_4")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_4_0(b, l + 1);
        r = r && consumeToken(b, HS_TILDE);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_4_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_4_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_4_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ AT
    private static boolean varsym_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_5")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_5_0(b, l + 1);
        r = r && consumeToken(b, HS_AT);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_5_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_5_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ CONSYM_ID
    private static boolean varsym_6(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_6")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_6_0(b, l + 1);
        r = r && consumeToken(b, HS_CONSYM_ID);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_6_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_6_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_6_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+ VERTICAL_BAR
    private static boolean varsym_7(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_7")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = varsym_7_0(b, l + 1);
        r = r && consumeToken(b, HS_VERTICAL_BAR);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_7_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_7_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_7_0", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT DOT DOT+
    private static boolean varsym_8(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_8")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, HS_DOT, HS_DOT);
        r = r && varsym_8_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DOT+
    private static boolean varsym_8_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "varsym_8_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, HS_DOT);
        while (r) {
            int c = current_position_(b);
            if (!consumeToken(b, HS_DOT)) break;
            if (!empty_element_parsed_guard_(b, "varsym_8_2", c)) break;
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // WHERE LEFT_BRACE body RIGHT_BRACE?
    public static boolean where_clause(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "where_clause")) return false;
        if (!nextTokenIs(b, HS_WHERE)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, HS_WHERE_CLAUSE, null);
        r = consumeTokens(b, 1, HS_WHERE, HS_LEFT_BRACE);
        p = r; // pin = 1
        r = r && report_error_(b, body(b, l + 1));
        r = p && where_clause_3(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // RIGHT_BRACE?
    private static boolean where_clause_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "where_clause_3")) return false;
        consumeToken(b, HS_RIGHT_BRACE);
        return true;
    }

}
