// This is a generated file. Not intended for manual editing.
package com.powertuple.intellij.haskell.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;
import static com.powertuple.intellij.haskell.psi.HaskellParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class HaskellParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == HS_ANN_PRAGMA) {
      r = ann_pragma(b, 0);
    }
    else if (t == HS_CDECL) {
      r = cdecl(b, 0);
    }
    else if (t == HS_CFILES_PRAGMA) {
      r = cfiles_pragma(b, 0);
    }
    else if (t == HS_CLASS_DECLARATION) {
      r = class_declaration(b, 0);
    }
    else if (t == HS_CLAZZ) {
      r = clazz(b, 0);
    }
    else if (t == HS_CNAME) {
      r = cname(b, 0);
    }
    else if (t == HS_COMMENTS) {
      r = comments(b, 0);
    }
    else if (t == HS_CON_ID) {
      r = con_id(b, 0);
    }
    else if (t == HS_CON_SYM) {
      r = con_sym(b, 0);
    }
    else if (t == HS_CONSTR_1) {
      r = constr1(b, 0);
    }
    else if (t == HS_CONSTR_2) {
      r = constr2(b, 0);
    }
    else if (t == HS_CONSTR_3) {
      r = constr3(b, 0);
    }
    else if (t == HS_CONSTR_4) {
      r = constr4(b, 0);
    }
    else if (t == HS_CONTEXT) {
      r = context(b, 0);
    }
    else if (t == HS_CTYPE_PRAGMA) {
      r = ctype_pragma(b, 0);
    }
    else if (t == HS_DATA_DECLARATION) {
      r = data_declaration(b, 0);
    }
    else if (t == HS_DATA_DECLARATION_DERIVING) {
      r = data_declaration_deriving(b, 0);
    }
    else if (t == HS_DEFAULT_DECLARATION) {
      r = default_declaration(b, 0);
    }
    else if (t == HS_DEPRECATED_WARN_PRAGMA) {
      r = deprecated_warn_pragma(b, 0);
    }
    else if (t == HS_DERIVING_DECLARATION) {
      r = deriving_declaration(b, 0);
    }
    else if (t == HS_DOT_DOT_PARENS) {
      r = dot_dot_parens(b, 0);
    }
    else if (t == HS_DUMMY_HEADER_PRAGMA) {
      r = dummy_header_pragma(b, 0);
    }
    else if (t == HS_EXPORT) {
      r = export(b, 0);
    }
    else if (t == HS_EXPORTS) {
      r = exports(b, 0);
    }
    else if (t == HS_EXPRESSION) {
      r = expression(b, 0);
    }
    else if (t == HS_FIELDDECL) {
      r = fielddecl(b, 0);
    }
    else if (t == HS_FILE_HEADER) {
      r = file_header(b, 0);
    }
    else if (t == HS_FILE_HEADER_PRAGMA) {
      r = file_header_pragma(b, 0);
    }
    else if (t == HS_FIRST_LINE_EXPRESSION) {
      r = first_line_expression(b, 0);
    }
    else if (t == HS_FIXITY) {
      r = fixity(b, 0);
    }
    else if (t == HS_FOREIGN_DECLARATION) {
      r = foreign_declaration(b, 0);
    }
    else if (t == HS_GCON_SYM) {
      r = gcon_sym(b, 0);
    }
    else if (t == HS_GENERAL_PRAGMA_CONTENT) {
      r = general_pragma_content(b, 0);
    }
    else if (t == HS_GTYCON) {
      r = gtycon(b, 0);
    }
    else if (t == HS_HADDOCK_PRAGMA) {
      r = haddock_pragma(b, 0);
    }
    else if (t == HS_IDECL) {
      r = idecl(b, 0);
    }
    else if (t == HS_IMPORT_DECLARATION) {
      r = import_declaration(b, 0);
    }
    else if (t == HS_IMPORT_EMPTY_SPEC) {
      r = import_empty_spec(b, 0);
    }
    else if (t == HS_IMPORT_HIDING) {
      r = import_hiding(b, 0);
    }
    else if (t == HS_IMPORT_HIDING_SPEC) {
      r = import_hiding_spec(b, 0);
    }
    else if (t == HS_IMPORT_ID) {
      r = import_id(b, 0);
    }
    else if (t == HS_IMPORT_IDS_SPEC) {
      r = import_ids_spec(b, 0);
    }
    else if (t == HS_IMPORT_MODULE) {
      r = import_module(b, 0);
    }
    else if (t == HS_IMPORT_QUALIFIED) {
      r = import_qualified(b, 0);
    }
    else if (t == HS_IMPORT_QUALIFIED_AS) {
      r = import_qualified_as(b, 0);
    }
    else if (t == HS_IMPORT_SPEC) {
      r = import_spec(b, 0);
    }
    else if (t == HS_INCLUDE_PRAGMA) {
      r = include_pragma(b, 0);
    }
    else if (t == HS_INLINABLE_PRAGMA) {
      r = inlinable_pragma(b, 0);
    }
    else if (t == HS_INLINE_PRAGMA) {
      r = inline_pragma(b, 0);
    }
    else if (t == HS_INST) {
      r = inst(b, 0);
    }
    else if (t == HS_INSTANCE_DECLARATION) {
      r = instance_declaration(b, 0);
    }
    else if (t == HS_INSTVAR) {
      r = instvar(b, 0);
    }
    else if (t == HS_LANGUAGE_PRAGMA) {
      r = language_pragma(b, 0);
    }
    else if (t == HS_LAST_LINE_EXPRESSION) {
      r = last_line_expression(b, 0);
    }
    else if (t == HS_LINE_EXPRESSION) {
      r = line_expression(b, 0);
    }
    else if (t == HS_LINE_PRAGMA) {
      r = line_pragma(b, 0);
    }
    else if (t == HS_LITERAL) {
      r = literal(b, 0);
    }
    else if (t == HS_MINIMAL_PRAGMA) {
      r = minimal_pragma(b, 0);
    }
    else if (t == HS_MOD_ID) {
      r = mod_id(b, 0);
    }
    else if (t == HS_MODULE_BODY) {
      r = module_body(b, 0);
    }
    else if (t == HS_MODULE_DECLARATION) {
      r = module_declaration(b, 0);
    }
    else if (t == HS_NEWCONSTR) {
      r = newconstr(b, 0);
    }
    else if (t == HS_NEWCONSTR_FIELDDECL) {
      r = newconstr_fielddecl(b, 0);
    }
    else if (t == HS_NEWTYPE_DECLARATION) {
      r = newtype_declaration(b, 0);
    }
    else if (t == HS_NOINLINE_PRAGMA) {
      r = noinline_pragma(b, 0);
    }
    else if (t == HS_NOUNPACK_PRAGMA) {
      r = nounpack_pragma(b, 0);
    }
    else if (t == HS_OP) {
      r = op(b, 0);
    }
    else if (t == HS_OPS) {
      r = ops(b, 0);
    }
    else if (t == HS_OPTIONS_GHC_PRAGMA) {
      r = options_ghc_pragma(b, 0);
    }
    else if (t == HS_OTHER_PRAGMA) {
      r = other_pragma(b, 0);
    }
    else if (t == HS_PARALLEL_ARRAY_TYPE) {
      r = parallel_array_type(b, 0);
    }
    else if (t == HS_QCON) {
      r = qcon(b, 0);
    }
    else if (t == HS_QCON_ID) {
      r = qcon_id(b, 0);
    }
    else if (t == HS_QCON_ID_QUALIFIER) {
      r = qcon_id_qualifier(b, 0);
    }
    else if (t == HS_QCON_OP) {
      r = qcon_op(b, 0);
    }
    else if (t == HS_QCON_SYM) {
      r = qcon_sym(b, 0);
    }
    else if (t == HS_QQ_EXPRESSION) {
      r = qq_expression(b, 0);
    }
    else if (t == HS_QUALIFIER) {
      r = qualifier(b, 0);
    }
    else if (t == HS_QUASI_QUOTE) {
      r = quasi_quote(b, 0);
    }
    else if (t == HS_QVAR) {
      r = qvar(b, 0);
    }
    else if (t == HS_QVAR_DOT_SYM) {
      r = qvar_dot_sym(b, 0);
    }
    else if (t == HS_QVAR_ID) {
      r = qvar_id(b, 0);
    }
    else if (t == HS_QVAR_OP) {
      r = qvar_op(b, 0);
    }
    else if (t == HS_QVAR_SYM) {
      r = qvar_sym(b, 0);
    }
    else if (t == HS_RULES_PRAGMA) {
      r = rules_pragma(b, 0);
    }
    else if (t == HS_SCONTEXT) {
      r = scontext(b, 0);
    }
    else if (t == HS_SIMPLECLASS) {
      r = simpleclass(b, 0);
    }
    else if (t == HS_SIMPLECLASS_TILDE_PART) {
      r = simpleclass_tilde_part(b, 0);
    }
    else if (t == HS_SIMPLETYPE) {
      r = simpletype(b, 0);
    }
    else if (t == HS_SNL) {
      r = snl(b, 0);
    }
    else if (t == HS_SOURCE_PRAGMA) {
      r = source_pragma(b, 0);
    }
    else if (t == HS_SPECIALIZE_PRAGMA) {
      r = specialize_pragma(b, 0);
    }
    else if (t == HS_SUB_CONSTR_2) {
      r = sub_constr2(b, 0);
    }
    else if (t == HS_TOP_DECLARATION) {
      r = top_declaration(b, 0);
    }
    else if (t == HS_TTYPE) {
      r = ttype(b, 0);
    }
    else if (t == HS_TYPE_DECLARATION) {
      r = type_declaration(b, 0);
    }
    else if (t == HS_TYPE_FAMILY_DECLARATION) {
      r = type_family_declaration(b, 0);
    }
    else if (t == HS_TYPE_FAMILY_TYPE) {
      r = type_family_type(b, 0);
    }
    else if (t == HS_TYPE_FAMILY_TYPE_1) {
      r = type_family_type1(b, 0);
    }
    else if (t == HS_TYPE_FAMILY_TYPE_2) {
      r = type_family_type2(b, 0);
    }
    else if (t == HS_TYPE_INSTANCE_DECLARATION) {
      r = type_instance_declaration(b, 0);
    }
    else if (t == HS_TYPE_SIGNATURE_DECLARATION) {
      r = type_signature_declaration(b, 0);
    }
    else if (t == HS_UNPACK_NOUNPACK_PRAGMA) {
      r = unpack_nounpack_pragma(b, 0);
    }
    else if (t == HS_UNPACK_PRAGMA) {
      r = unpack_pragma(b, 0);
    }
    else if (t == HS_VAR_DOT_SYM) {
      r = var_dot_sym(b, 0);
    }
    else if (t == HS_VAR_ID) {
      r = var_id(b, 0);
    }
    else if (t == HS_VAR_SYM) {
      r = var_sym(b, 0);
    }
    else if (t == HS_VARS) {
      r = vars(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return program(b, l + 1);
  }

  /* ********************************************************** */
  // PRAGMA_START onl "ANN" general_pragma_content PRAGMA_END
  public static boolean ann_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ann_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "ANN");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_ANN_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // qvar (qvar | ttype | LEFT_PAREN type_signature_declaration RIGHT_PAREN)* (DOUBLE_RIGHT_ARROW ttype)? |  // this is forall declaration, so first qvar is 'forall'
  //                                   LEFT_PAREN qvar TILDE qvar RIGHT_PAREN (DOUBLE_RIGHT_ARROW ttype)? |
  //                                   LEFT_PAREN osnl ttype (osnl COMMA osnl ttype)+ osnl RIGHT_PAREN |
  //                                   LEFT_BRACKET osnl ttype osnl RIGHT_BRACKET |
  //                                   LEFT_PAREN osnl ttype+ osnl RIGHT_PAREN |
  //                                   gtycon |
  //                                   qvar |
  //                                   qvar_op
  static boolean atype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_0(b, l + 1);
    if (!r) r = atype_1(b, l + 1);
    if (!r) r = atype_2(b, l + 1);
    if (!r) r = atype_3(b, l + 1);
    if (!r) r = atype_4(b, l + 1);
    if (!r) r = gtycon(b, l + 1);
    if (!r) r = qvar(b, l + 1);
    if (!r) r = qvar_op(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar (qvar | ttype | LEFT_PAREN type_signature_declaration RIGHT_PAREN)* (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean atype_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    r = r && atype_0_1(b, l + 1);
    r = r && atype_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (qvar | ttype | LEFT_PAREN type_signature_declaration RIGHT_PAREN)*
  private static boolean atype_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!atype_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // qvar | ttype | LEFT_PAREN type_signature_declaration RIGHT_PAREN
  private static boolean atype_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    if (!r) r = ttype(b, l + 1);
    if (!r) r = atype_0_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN type_signature_declaration RIGHT_PAREN
  private static boolean atype_0_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && type_signature_declaration(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean atype_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_2")) return false;
    atype_0_2_0(b, l + 1);
    return true;
  }

  // DOUBLE_RIGHT_ARROW ttype
  private static boolean atype_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN qvar TILDE qvar RIGHT_PAREN (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean atype_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && qvar(b, l + 1);
    r = r && consumeToken(b, HS_TILDE);
    r = r && qvar(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    r = r && atype_1_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean atype_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_1_5")) return false;
    atype_1_5_0(b, l + 1);
    return true;
  }

  // DOUBLE_RIGHT_ARROW ttype
  private static boolean atype_1_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_1_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl ttype (osnl COMMA osnl ttype)+ osnl RIGHT_PAREN
  private static boolean atype_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && atype_2_3(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl ttype)+
  private static boolean atype_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_2_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!atype_2_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_2_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // osnl COMMA osnl ttype
  private static boolean atype_2_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACKET osnl ttype osnl RIGHT_BRACKET
  private static boolean atype_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl ttype+ osnl RIGHT_PAREN
  private static boolean atype_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && atype_4_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype+
  private static boolean atype_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ttype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_4_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // import_declarations top_declarations
  static boolean body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "body")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declarations(b, l + 1);
    r = r && top_declarations(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // atype+
  static boolean btype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "btype")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!atype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "btype", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // minimal_pragma | type_signature_declaration | cidecl
  public static boolean cdecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cdecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<cdecl>");
    r = minimal_pragma(b, l + 1);
    if (!r) r = type_signature_declaration(b, l + 1);
    if (!r) r = cidecl(b, l + 1);
    exit_section_(b, l, m, HS_CDECL, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // cdecl+ | expression
  static boolean cdecls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cdecls")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cdecls_0(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // cdecl+
  private static boolean cdecls_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cdecls_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cdecl(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!cdecl(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "cdecls_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "CFILES" general_pragma_content PRAGMA_END
  public static boolean cfiles_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cfiles_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "CFILES");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_CFILES_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // inline_pragma | noinline_pragma | specialize_pragma | type_declaration | instance_declaration | default_declaration |
  //                                   newtype_declaration | data_declaration
  static boolean cidecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cidecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_pragma(b, l + 1);
    if (!r) r = noinline_pragma(b, l + 1);
    if (!r) r = specialize_pragma(b, l + 1);
    if (!r) r = type_declaration(b, l + 1);
    if (!r) r = instance_declaration(b, l + 1);
    if (!r) r = default_declaration(b, l + 1);
    if (!r) r = newtype_declaration(b, l + 1);
    if (!r) r = data_declaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CLASS osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl
  //                                   (qvar | LEFT_PAREN type_signature_declaration RIGHT_PAREN)*
  //                                   (osnl VERTICAL_BAR osnl ttype (osnl COMMA osnl ttype)*)? osnl WHERE? cdecls? |
  //                                   CLASS osnl context osnl DOUBLE_RIGHT_ARROW osnl qcon osnl qvar osnl WHERE? cdecls?
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

  // CLASS osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl
  //                                   (qvar | LEFT_PAREN type_signature_declaration RIGHT_PAREN)*
  //                                   (osnl VERTICAL_BAR osnl ttype (osnl COMMA osnl ttype)*)? osnl WHERE? cdecls?
  private static boolean class_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CLASS);
    r = r && osnl(b, l + 1);
    r = r && class_declaration_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && class_declaration_0_6(b, l + 1);
    r = r && class_declaration_0_7(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && class_declaration_0_9(b, l + 1);
    r = r && class_declaration_0_10(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext osnl DOUBLE_RIGHT_ARROW)?
  private static boolean class_declaration_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_2")) return false;
    class_declaration_0_2_0(b, l + 1);
    return true;
  }

  // scontext osnl DOUBLE_RIGHT_ARROW
  private static boolean class_declaration_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (qvar | LEFT_PAREN type_signature_declaration RIGHT_PAREN)*
  private static boolean class_declaration_0_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!class_declaration_0_6_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "class_declaration_0_6", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // qvar | LEFT_PAREN type_signature_declaration RIGHT_PAREN
  private static boolean class_declaration_0_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    if (!r) r = class_declaration_0_6_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN type_signature_declaration RIGHT_PAREN
  private static boolean class_declaration_0_6_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && type_signature_declaration(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl VERTICAL_BAR osnl ttype (osnl COMMA osnl ttype)*)?
  private static boolean class_declaration_0_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7")) return false;
    class_declaration_0_7_0(b, l + 1);
    return true;
  }

  // osnl VERTICAL_BAR osnl ttype (osnl COMMA osnl ttype)*
  private static boolean class_declaration_0_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_VERTICAL_BAR);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && class_declaration_0_7_0_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl ttype)*
  private static boolean class_declaration_0_7_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7_0_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!class_declaration_0_7_0_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "class_declaration_0_7_0_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl COMMA osnl ttype
  private static boolean class_declaration_0_7_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WHERE?
  private static boolean class_declaration_0_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_9")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  // cdecls?
  private static boolean class_declaration_0_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_10")) return false;
    cdecls(b, l + 1);
    return true;
  }

  // CLASS osnl context osnl DOUBLE_RIGHT_ARROW osnl qcon osnl qvar osnl WHERE? cdecls?
  private static boolean class_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CLASS);
    r = r && osnl(b, l + 1);
    r = r && context(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    r = r && osnl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qvar(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && class_declaration_1_10(b, l + 1);
    r = r && class_declaration_1_11(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WHERE?
  private static boolean class_declaration_1_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_1_10")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  // cdecls?
  private static boolean class_declaration_1_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_1_11")) return false;
    cdecls(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // qcon qvar+ |
  //                                   qcon LEFT_PAREN qvar atype+ RIGHT_PAREN |
  //                                   qcon LEFT_PAREN qcon qvar RIGHT_PAREN
  public static boolean clazz(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<clazz>");
    r = clazz_0(b, l + 1);
    if (!r) r = clazz_1(b, l + 1);
    if (!r) r = clazz_2(b, l + 1);
    exit_section_(b, l, m, HS_CLAZZ, r, false, null);
    return r;
  }

  // qcon qvar+
  private static boolean clazz_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && clazz_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar+
  private static boolean clazz_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon LEFT_PAREN qvar atype+ RIGHT_PAREN
  private static boolean clazz_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && qvar(b, l + 1);
    r = r && clazz_1_3(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // atype+
  private static boolean clazz_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!atype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_1_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon LEFT_PAREN qcon qvar RIGHT_PAREN
  private static boolean clazz_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && qcon(b, l + 1);
    r = r && qvar(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qvar | qcon
  public static boolean cname(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cname")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<cname>");
    r = qvar(b, l + 1);
    if (!r) r = qcon(b, l + 1);
    exit_section_(b, l, m, HS_CNAME, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMENT | NCOMMENT | NCOMMENT_START | NCOMMENT_END
  public static boolean comments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<comments>");
    r = consumeToken(b, HS_COMMENT);
    if (!r) r = consumeToken(b, HS_NCOMMENT);
    if (!r) r = consumeToken(b, HS_NCOMMENT_START);
    if (!r) r = consumeToken(b, HS_NCOMMENT_END);
    exit_section_(b, l, m, HS_COMMENTS, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CONID_ID | LEFT_BRACKET var_id? RIGHT_BRACKET | LEFT_PAREN COMMA* RIGHT_PAREN
  public static boolean con_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_id")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<con id>");
    r = consumeToken(b, HS_CONID_ID);
    if (!r) r = con_id_1(b, l + 1);
    if (!r) r = con_id_2(b, l + 1);
    exit_section_(b, l, m, HS_CON_ID, r, false, null);
    return r;
  }

  // LEFT_BRACKET var_id? RIGHT_BRACKET
  private static boolean con_id_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_id_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && con_id_1_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // var_id?
  private static boolean con_id_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_id_1_1")) return false;
    var_id(b, l + 1);
    return true;
  }

  // LEFT_PAREN COMMA* RIGHT_PAREN
  private static boolean con_id_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_id_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && con_id_2_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA*
  private static boolean con_id_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_id_2_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, HS_COMMA)) break;
      if (!empty_element_parsed_guard_(b, "con_id_2_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // CONSYM_ID
  public static boolean con_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_sym")) return false;
    if (!nextTokenIs(b, HS_CONSYM_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CONSYM_ID);
    exit_section_(b, m, HS_CON_SYM, r);
    return r;
  }

  /* ********************************************************** */
  // constr1 | constr2 | constr3 | constr4
  static boolean constr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constr1(b, l + 1);
    if (!r) r = constr2(b, l + 1);
    if (!r) r = constr3(b, l + 1);
    if (!r) r = constr4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? qcon onl unpack_nounpack_pragma? onl LEFT_BRACE onl (onl fielddecl (onl COMMA? onl fielddecl)*)? onl RIGHT_BRACE
  public static boolean constr1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<constr 1>");
    r = constr1_0(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && constr1_3(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_BRACE);
    r = r && onl(b, l + 1);
    r = r && constr1_7(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACE);
    exit_section_(b, l, m, HS_CONSTR_1, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // unpack_nounpack_pragma?
  private static boolean constr1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_3")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // (onl fielddecl (onl COMMA? onl fielddecl)*)?
  private static boolean constr1_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7")) return false;
    constr1_7_0(b, l + 1);
    return true;
  }

  // onl fielddecl (onl COMMA? onl fielddecl)*
  private static boolean constr1_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && fielddecl(b, l + 1);
    r = r && constr1_7_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onl COMMA? onl fielddecl)*
  private static boolean constr1_7_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constr1_7_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr1_7_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA? onl fielddecl
  private static boolean constr1_7_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && constr1_7_0_2_0_1(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && fielddecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean constr1_7_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_2_0_1")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? sub_constr2 osnl unpack_nounpack_pragma? qcon_op osnl sub_constr2
  public static boolean constr2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<constr 2>");
    r = constr2_0(b, l + 1);
    r = r && sub_constr2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && constr2_3(b, l + 1);
    r = r && qcon_op(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && sub_constr2(b, l + 1);
    exit_section_(b, l, m, HS_CONSTR_2, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // unpack_nounpack_pragma?
  private static boolean constr2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_3")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? qcon osnl (unpack_nounpack_pragma? qvar_op? osnl atype osnl)*
  public static boolean constr3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<constr 3>");
    r = constr3_0(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && constr3_3(b, l + 1);
    exit_section_(b, l, m, HS_CONSTR_3, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // (unpack_nounpack_pragma? qvar_op? osnl atype osnl)*
  private static boolean constr3_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constr3_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr3_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // unpack_nounpack_pragma? qvar_op? osnl atype osnl
  private static boolean constr3_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constr3_3_0_0(b, l + 1);
    r = r && constr3_3_0_1(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && atype(b, l + 1);
    r = r && osnl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr3_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_3_0_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // qvar_op?
  private static boolean constr3_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_3_0_1")) return false;
    qvar_op(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? qvar gcon_sym unpack_nounpack_pragma? qcon
  public static boolean constr4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<constr 4>");
    r = constr4_0(b, l + 1);
    r = r && qvar(b, l + 1);
    r = r && gcon_sym(b, l + 1);
    r = r && constr4_3(b, l + 1);
    r = r && qcon(b, l + 1);
    exit_section_(b, l, m, HS_CONSTR_4, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // unpack_nounpack_pragma?
  private static boolean constr4_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4_3")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // constr (osnl VERTICAL_BAR osnl constr)*
  static boolean constrs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constr(b, l + 1);
    r = r && constrs_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl VERTICAL_BAR osnl constr)*
  private static boolean constrs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constrs_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constrs_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl VERTICAL_BAR osnl constr
  private static boolean constrs_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_VERTICAL_BAR);
    r = r && osnl(b, l + 1);
    r = r && constr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PAREN osnl (clazz (osnl COMMA osnl clazz)*)? osnl RIGHT_PAREN |
  //                                   clazz
  public static boolean context(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<context>");
    r = context_0(b, l + 1);
    if (!r) r = clazz(b, l + 1);
    exit_section_(b, l, m, HS_CONTEXT, r, false, null);
    return r;
  }

  // LEFT_PAREN osnl (clazz (osnl COMMA osnl clazz)*)? osnl RIGHT_PAREN
  private static boolean context_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && context_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (clazz (osnl COMMA osnl clazz)*)?
  private static boolean context_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context_0_2")) return false;
    context_0_2_0(b, l + 1);
    return true;
  }

  // clazz (osnl COMMA osnl clazz)*
  private static boolean context_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = clazz(b, l + 1);
    r = r && context_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl clazz)*
  private static boolean context_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context_0_2_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!context_0_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "context_0_2_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl COMMA osnl clazz
  private static boolean context_0_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "context_0_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && clazz(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "CTYPE" general_pragma_content PRAGMA_END
  public static boolean ctype_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ctype_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "CTYPE");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_CTYPE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // DATA osnl ctype_pragma? osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype osnl EQUAL osnl constrs osnl data_declaration_deriving? |
  //                                   DATA osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype osnl WHERE cdecls osnl data_declaration_deriving? |
  //                                   DATA osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype (EQUAL expression)?
  public static boolean data_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration")) return false;
    if (!nextTokenIs(b, HS_DATA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = data_declaration_0(b, l + 1);
    if (!r) r = data_declaration_1(b, l + 1);
    if (!r) r = data_declaration_2(b, l + 1);
    exit_section_(b, m, HS_DATA_DECLARATION, r);
    return r;
  }

  // DATA osnl ctype_pragma? osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype osnl EQUAL osnl constrs osnl data_declaration_deriving?
  private static boolean data_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DATA);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_0_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_0_6(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_EQUAL);
    r = r && osnl(b, l + 1);
    r = r && constrs(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_0_14(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ctype_pragma?
  private static boolean data_declaration_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_2")) return false;
    ctype_pragma(b, l + 1);
    return true;
  }

  // INSTANCE?
  private static boolean data_declaration_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_4")) return false;
    consumeToken(b, HS_INSTANCE);
    return true;
  }

  // (context osnl DOUBLE_RIGHT_ARROW)?
  private static boolean data_declaration_0_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_6")) return false;
    data_declaration_0_6_0(b, l + 1);
    return true;
  }

  // context osnl DOUBLE_RIGHT_ARROW
  private static boolean data_declaration_0_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = context(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // data_declaration_deriving?
  private static boolean data_declaration_0_14(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_14")) return false;
    data_declaration_deriving(b, l + 1);
    return true;
  }

  // DATA osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype osnl WHERE cdecls osnl data_declaration_deriving?
  private static boolean data_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DATA);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_1_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_1_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_WHERE);
    r = r && cdecls(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_1_11(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // INSTANCE?
  private static boolean data_declaration_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_2")) return false;
    consumeToken(b, HS_INSTANCE);
    return true;
  }

  // (context osnl DOUBLE_RIGHT_ARROW)?
  private static boolean data_declaration_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_4")) return false;
    data_declaration_1_4_0(b, l + 1);
    return true;
  }

  // context osnl DOUBLE_RIGHT_ARROW
  private static boolean data_declaration_1_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = context(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // data_declaration_deriving?
  private static boolean data_declaration_1_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_11")) return false;
    data_declaration_deriving(b, l + 1);
    return true;
  }

  // DATA osnl INSTANCE? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype (EQUAL expression)?
  private static boolean data_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DATA);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_2_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && data_declaration_2_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && data_declaration_2_7(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // INSTANCE?
  private static boolean data_declaration_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2_2")) return false;
    consumeToken(b, HS_INSTANCE);
    return true;
  }

  // (context osnl DOUBLE_RIGHT_ARROW)?
  private static boolean data_declaration_2_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2_4")) return false;
    data_declaration_2_4_0(b, l + 1);
    return true;
  }

  // context osnl DOUBLE_RIGHT_ARROW
  private static boolean data_declaration_2_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = context(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (EQUAL expression)?
  private static boolean data_declaration_2_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2_7")) return false;
    data_declaration_2_7_0(b, l + 1);
    return true;
  }

  // EQUAL expression
  private static boolean data_declaration_2_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_2_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_EQUAL);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (DERIVING ttype osnl | DERIVING LEFT_PAREN ttype (osnl COMMA osnl ttype)+ RIGHT_PAREN osnl)+
  public static boolean data_declaration_deriving(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving")) return false;
    if (!nextTokenIs(b, HS_DERIVING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = data_declaration_deriving_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!data_declaration_deriving_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_deriving", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, HS_DATA_DECLARATION_DERIVING, r);
    return r;
  }

  // DERIVING ttype osnl | DERIVING LEFT_PAREN ttype (osnl COMMA osnl ttype)+ RIGHT_PAREN osnl
  private static boolean data_declaration_deriving_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = data_declaration_deriving_0_0(b, l + 1);
    if (!r) r = data_declaration_deriving_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DERIVING ttype osnl
  private static boolean data_declaration_deriving_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DERIVING);
    r = r && ttype(b, l + 1);
    r = r && osnl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DERIVING LEFT_PAREN ttype (osnl COMMA osnl ttype)+ RIGHT_PAREN osnl
  private static boolean data_declaration_deriving_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_DERIVING, HS_LEFT_PAREN);
    r = r && ttype(b, l + 1);
    r = r && data_declaration_deriving_0_1_3(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    r = r && osnl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl ttype)+
  private static boolean data_declaration_deriving_0_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = data_declaration_deriving_0_1_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!data_declaration_deriving_0_1_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_deriving_0_1_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // osnl COMMA osnl ttype
  private static boolean data_declaration_deriving_0_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DEFAULT osnl (LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN | type_signature_declaration)
  public static boolean default_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration")) return false;
    if (!nextTokenIs(b, HS_DEFAULT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DEFAULT);
    r = r && osnl(b, l + 1);
    r = r && default_declaration_2(b, l + 1);
    exit_section_(b, m, HS_DEFAULT_DECLARATION, r);
    return r;
  }

  // LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN | type_signature_declaration
  private static boolean default_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = default_declaration_2_0(b, l + 1);
    if (!r) r = type_signature_declaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN
  private static boolean default_declaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && default_declaration_2_0_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ttype (COMMA ttype)*)?
  private static boolean default_declaration_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2_0_1")) return false;
    default_declaration_2_0_1_0(b, l + 1);
    return true;
  }

  // ttype (COMMA ttype)*
  private static boolean default_declaration_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    r = r && default_declaration_2_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ttype)*
  private static boolean default_declaration_2_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2_0_1_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!default_declaration_2_0_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "default_declaration_2_0_1_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA ttype
  private static boolean default_declaration_2_0_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2_0_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("DEPRECATED" | "WARNING") general_pragma_content PRAGMA_END
  public static boolean deprecated_warn_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecated_warn_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && deprecated_warn_pragma_2(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_DEPRECATED_WARN_PRAGMA, r);
    return r;
  }

  // "DEPRECATED" | "WARNING"
  private static boolean deprecated_warn_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecated_warn_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "DEPRECATED");
    if (!r) r = consumeToken(b, "WARNING");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DERIVING INSTANCE (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl inst
  public static boolean deriving_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deriving_declaration")) return false;
    if (!nextTokenIs(b, HS_DERIVING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_DERIVING, HS_INSTANCE);
    r = r && deriving_declaration_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && inst(b, l + 1);
    exit_section_(b, m, HS_DERIVING_DECLARATION, r);
    return r;
  }

  // (scontext osnl DOUBLE_RIGHT_ARROW)?
  private static boolean deriving_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deriving_declaration_2")) return false;
    deriving_declaration_2_0(b, l + 1);
    return true;
  }

  // scontext osnl DOUBLE_RIGHT_ARROW
  private static boolean deriving_declaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deriving_declaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PAREN DOT DOT RIGHT_PAREN
  public static boolean dot_dot_parens(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dot_dot_parens")) return false;
    if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_LEFT_PAREN, HS_DOT, HS_DOT, HS_RIGHT_PAREN);
    exit_section_(b, m, HS_DOT_DOT_PARENS, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START CONID_ID? PRAGMA_END? NEWLINE
  public static boolean dummy_header_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_header_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && dummy_header_pragma_1(b, l + 1);
    r = r && dummy_header_pragma_2(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, HS_DUMMY_HEADER_PRAGMA, r);
    return r;
  }

  // CONID_ID?
  private static boolean dummy_header_pragma_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_header_pragma_1")) return false;
    consumeToken(b, HS_CONID_ID);
    return true;
  }

  // PRAGMA_END?
  private static boolean dummy_header_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_header_pragma_2")) return false;
    consumeToken(b, HS_PRAGMA_END);
    return true;
  }

  /* ********************************************************** */
  // export1 | export2 | export3
  public static boolean export(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<export>");
    r = export1(b, l + 1);
    if (!r) r = export2(b, l + 1);
    if (!r) r = export3(b, l + 1);
    exit_section_(b, l, m, HS_EXPORT, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TYPE? onl (qvar | qvar_op)
  static boolean export1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = export1_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TYPE?
  private static boolean export1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export1_0")) return false;
    consumeToken(b, HS_TYPE);
    return true;
  }

  // qvar | qvar_op
  private static boolean export1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    if (!r) r = qvar_op(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon onl (dot_dot_parens | LEFT_PAREN onl (cname onl (onl COMMA onl cname)*)? onl RIGHT_PAREN)?
  static boolean export2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (dot_dot_parens | LEFT_PAREN onl (cname onl (onl COMMA onl cname)*)? onl RIGHT_PAREN)?
  private static boolean export2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2")) return false;
    export2_2_0(b, l + 1);
    return true;
  }

  // dot_dot_parens | LEFT_PAREN onl (cname onl (onl COMMA onl cname)*)? onl RIGHT_PAREN
  private static boolean export2_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = dot_dot_parens(b, l + 1);
    if (!r) r = export2_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN onl (cname onl (onl COMMA onl cname)*)? onl RIGHT_PAREN
  private static boolean export2_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && onl(b, l + 1);
    r = r && export2_2_0_1_2(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (cname onl (onl COMMA onl cname)*)?
  private static boolean export2_2_0_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0_1_2")) return false;
    export2_2_0_1_2_0(b, l + 1);
    return true;
  }

  // cname onl (onl COMMA onl cname)*
  private static boolean export2_2_0_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export2_2_0_1_2_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onl COMMA onl cname)*
  private static boolean export2_2_0_1_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0_1_2_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!export2_2_0_1_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "export2_2_0_1_2_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA onl cname
  private static boolean export2_2_0_1_2_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2_0_1_2_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && onl(b, l + 1);
    r = r && cname(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MODULE mod_id
  static boolean export3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3")) return false;
    if (!nextTokenIs(b, HS_MODULE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_MODULE);
    r = r && mod_id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PAREN onl export? (onl COMMA? onl export onl)* onl COMMA? onl RIGHT_PAREN
  public static boolean exports(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports")) return false;
    if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && onl(b, l + 1);
    r = r && exports_2(b, l + 1);
    r = r && exports_3(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && exports_5(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, HS_EXPORTS, r);
    return r;
  }

  // export?
  private static boolean exports_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_2")) return false;
    export(b, l + 1);
    return true;
  }

  // (onl COMMA? onl export onl)*
  private static boolean exports_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!exports_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exports_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA? onl export onl
  private static boolean exports_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && exports_3_0_1(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean exports_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_3_0_1")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  // COMMA?
  private static boolean exports_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_5")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  /* ********************************************************** */
  // first_line_expression (line_expression)* last_line_expression | last_line_expression
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = expression_0(b, l + 1);
    if (!r) r = last_line_expression(b, l + 1);
    exit_section_(b, l, m, HS_EXPRESSION, r, false, null);
    return r;
  }

  // first_line_expression (line_expression)* last_line_expression
  private static boolean expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = first_line_expression(b, l + 1);
    r = r && expression_0_1(b, l + 1);
    r = r && last_line_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (line_expression)*
  private static boolean expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!expression_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (line_expression)
  private static boolean expression_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = line_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // vars (COLON_COLON unpack_nounpack_pragma? (ttype | qvar_op atype))?
  public static boolean fielddecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<fielddecl>");
    r = vars(b, l + 1);
    r = r && fielddecl_1(b, l + 1);
    exit_section_(b, l, m, HS_FIELDDECL, r, false, null);
    return r;
  }

  // (COLON_COLON unpack_nounpack_pragma? (ttype | qvar_op atype))?
  private static boolean fielddecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1")) return false;
    fielddecl_1_0(b, l + 1);
    return true;
  }

  // COLON_COLON unpack_nounpack_pragma? (ttype | qvar_op atype)
  private static boolean fielddecl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COLON_COLON);
    r = r && fielddecl_1_0_1(b, l + 1);
    r = r && fielddecl_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean fielddecl_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0_1")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // ttype | qvar_op atype
  private static boolean fielddecl_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    if (!r) r = fielddecl_1_0_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar_op atype
  private static boolean fielddecl_1_0_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar_op(b, l + 1);
    r = r && atype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SHEBANG_LINE? onl (file_header_pragma onl)+
  public static boolean file_header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<file header>");
    r = file_header_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && file_header_2(b, l + 1);
    exit_section_(b, l, m, HS_FILE_HEADER, r, false, null);
    return r;
  }

  // SHEBANG_LINE?
  private static boolean file_header_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_0")) return false;
    consumeToken(b, HS_SHEBANG_LINE);
    return true;
  }

  // (file_header_pragma onl)+
  private static boolean file_header_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = file_header_2_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!file_header_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file_header_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // file_header_pragma onl
  private static boolean file_header_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = file_header_pragma(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // language_pragma | options_ghc_pragma | include_pragma | haddock_pragma | ann_pragma | dummy_header_pragma
  public static boolean file_header_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = language_pragma(b, l + 1);
    if (!r) r = options_ghc_pragma(b, l + 1);
    if (!r) r = include_pragma(b, l + 1);
    if (!r) r = haddock_pragma(b, l + 1);
    if (!r) r = ann_pragma(b, l + 1);
    if (!r) r = dummy_header_pragma(b, l + 1);
    exit_section_(b, m, HS_FILE_HEADER_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // general_id+ snl
  public static boolean first_line_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_line_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<first line expression>");
    r = first_line_expression_0(b, l + 1);
    r = r && snl(b, l + 1);
    exit_section_(b, l, m, HS_FIRST_LINE_EXPRESSION, r, false, null);
    return r;
  }

  // general_id+
  private static boolean first_line_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_line_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "first_line_expression_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INFIXL | INFIXR | INFIX
  public static boolean fixity(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fixity")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<fixity>");
    r = consumeToken(b, HS_INFIXL);
    if (!r) r = consumeToken(b, HS_INFIXR);
    if (!r) r = consumeToken(b, HS_INFIX);
    exit_section_(b, l, m, HS_FIXITY, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (FOREIGN_IMPORT | FOREIGN_EXPORT) osnl expression
  public static boolean foreign_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreign_declaration")) return false;
    if (!nextTokenIs(b, "<foreign declaration>", HS_FOREIGN_EXPORT, HS_FOREIGN_IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<foreign declaration>");
    r = foreign_declaration_0(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, HS_FOREIGN_DECLARATION, r, false, null);
    return r;
  }

  // FOREIGN_IMPORT | FOREIGN_EXPORT
  private static boolean foreign_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreign_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_FOREIGN_IMPORT);
    if (!r) r = consumeToken(b, HS_FOREIGN_EXPORT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // con_sym | qcon_sym
  public static boolean gcon_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gcon_sym")) return false;
    if (!nextTokenIs(b, "<gcon sym>", HS_CONID_ID, HS_CONSYM_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<gcon sym>");
    r = con_sym(b, l + 1);
    if (!r) r = qcon_sym(b, l + 1);
    exit_section_(b, l, m, HS_GCON_SYM, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // qvar_op | qcon_op | qvar | qcon | LEFT_PAREN | RIGHT_PAREN | FLOAT | DO | WHERE | IF | THEN | ELSE |
  //                                   COLON_COLON | DOUBLE_RIGHT_ARROW | RIGHT_ARROW | IN | CASE | OF | LET |
  //                                   SEMICOLON | LEFT_ARROW | LEFT_BRACKET | RIGHT_BRACKET | literal | LEFT_BRACE | RIGHT_BRACE |
  //                                   COMMA | UNDERSCORE | symbol_reserved_op | QUOTE | BACKQUOTE | fixity
  static boolean general_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_id")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar_op(b, l + 1);
    if (!r) r = qcon_op(b, l + 1);
    if (!r) r = qvar(b, l + 1);
    if (!r) r = qcon(b, l + 1);
    if (!r) r = consumeToken(b, HS_LEFT_PAREN);
    if (!r) r = consumeToken(b, HS_RIGHT_PAREN);
    if (!r) r = consumeToken(b, HS_FLOAT);
    if (!r) r = consumeToken(b, HS_DO);
    if (!r) r = consumeToken(b, HS_WHERE);
    if (!r) r = consumeToken(b, HS_IF);
    if (!r) r = consumeToken(b, HS_THEN);
    if (!r) r = consumeToken(b, HS_ELSE);
    if (!r) r = consumeToken(b, HS_COLON_COLON);
    if (!r) r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    if (!r) r = consumeToken(b, HS_RIGHT_ARROW);
    if (!r) r = consumeToken(b, HS_IN);
    if (!r) r = consumeToken(b, HS_CASE);
    if (!r) r = consumeToken(b, HS_OF);
    if (!r) r = consumeToken(b, HS_LET);
    if (!r) r = consumeToken(b, HS_SEMICOLON);
    if (!r) r = consumeToken(b, HS_LEFT_ARROW);
    if (!r) r = consumeToken(b, HS_LEFT_BRACKET);
    if (!r) r = consumeToken(b, HS_RIGHT_BRACKET);
    if (!r) r = literal(b, l + 1);
    if (!r) r = consumeToken(b, HS_LEFT_BRACE);
    if (!r) r = consumeToken(b, HS_RIGHT_BRACE);
    if (!r) r = consumeToken(b, HS_COMMA);
    if (!r) r = consumeToken(b, HS_UNDERSCORE);
    if (!r) r = symbol_reserved_op(b, l + 1);
    if (!r) r = consumeToken(b, HS_QUOTE);
    if (!r) r = consumeToken(b, HS_BACKQUOTE);
    if (!r) r = fixity(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (general_id | NEWLINE | MODULE | INSTANCE)*
  public static boolean general_pragma_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_pragma_content")) return false;
    Marker m = enter_section_(b, l, _NONE_, "<general pragma content>");
    int c = current_position_(b);
    while (true) {
      if (!general_pragma_content_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "general_pragma_content", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, HS_GENERAL_PRAGMA_CONTENT, true, false, null);
    return true;
  }

  // general_id | NEWLINE | MODULE | INSTANCE
  private static boolean general_pragma_content_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_pragma_content_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    if (!r) r = consumeToken(b, HS_NEWLINE);
    if (!r) r = consumeToken(b, HS_MODULE);
    if (!r) r = consumeToken(b, HS_INSTANCE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN | QUOTE? LEFT_BRACKET RIGHT_BRACKET |LEFT_PAREN COMMA (COMMA)* RIGHT_PAREN
  public static boolean gtycon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gtycon")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<gtycon>");
    r = qcon(b, l + 1);
    if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_ARROW, HS_RIGHT_PAREN);
    if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
    if (!r) r = gtycon_3(b, l + 1);
    if (!r) r = gtycon_4(b, l + 1);
    exit_section_(b, l, m, HS_GTYCON, r, false, null);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, HS_COMMA)) break;
      if (!empty_element_parsed_guard_(b, "gtycon_4_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "OPTIONS_HADDOCK" general_pragma_content PRAGMA_END
  public static boolean haddock_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "OPTIONS_HADDOCK");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_HADDOCK_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // onl cidecl
  public static boolean idecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "idecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<idecl>");
    r = onl(b, l + 1);
    r = r && cidecl(b, l + 1);
    exit_section_(b, l, m, HS_IDECL, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // idecl+ | expression
  static boolean idecls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "idecls")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = idecls_0(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // idecl+
  private static boolean idecls_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "idecls_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = idecl(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!idecl(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "idecls_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT osnl source_pragma? osnl import_qualified? osnl import_module osnl import_qualified_as? osnl import_spec? NEWLINE
  public static boolean import_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration")) return false;
    if (!nextTokenIs(b, HS_IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_IMPORT);
    r = r && osnl(b, l + 1);
    r = r && import_declaration_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_declaration_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_module(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_declaration_8(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_declaration_10(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, HS_IMPORT_DECLARATION, r);
    return r;
  }

  // source_pragma?
  private static boolean import_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_2")) return false;
    source_pragma(b, l + 1);
    return true;
  }

  // import_qualified?
  private static boolean import_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_4")) return false;
    import_qualified(b, l + 1);
    return true;
  }

  // import_qualified_as?
  private static boolean import_declaration_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_8")) return false;
    import_qualified_as(b, l + 1);
    return true;
  }

  // import_spec?
  private static boolean import_declaration_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_10")) return false;
    import_spec(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ((import_declaration | cfiles_pragma) onl)*
  static boolean import_declarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations")) return false;
    int c = current_position_(b);
    while (true) {
      if (!import_declarations_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_declarations", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (import_declaration | cfiles_pragma) onl
  private static boolean import_declarations_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declarations_0_0(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // import_declaration | cfiles_pragma
  private static boolean import_declarations_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declaration(b, l + 1);
    if (!r) r = cfiles_pragma(b, l + 1);
    exit_section_(b, m, null, r);
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
    Marker m = enter_section_(b, l, _NONE_, "<import hiding>");
    r = consumeToken(b, "hiding");
    exit_section_(b, l, m, HS_IMPORT_HIDING, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // import_hiding osnl LEFT_PAREN osnl (import_id osnl (osnl COMMA osnl import_id)* osnl (COMMA)?)? osnl RIGHT_PAREN
  public static boolean import_hiding_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<import hiding spec>");
    r = import_hiding(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && import_hiding_spec_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, l, m, HS_IMPORT_HIDING_SPEC, r, false, null);
    return r;
  }

  // (import_id osnl (osnl COMMA osnl import_id)* osnl (COMMA)?)?
  private static boolean import_hiding_spec_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec_4")) return false;
    import_hiding_spec_4_0(b, l + 1);
    return true;
  }

  // import_id osnl (osnl COMMA osnl import_id)* osnl (COMMA)?
  private static boolean import_hiding_spec_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_id(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_hiding_spec_4_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_hiding_spec_4_0_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl import_id)*
  private static boolean import_hiding_spec_4_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec_4_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!import_hiding_spec_4_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_hiding_spec_4_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl COMMA osnl import_id
  private static boolean import_hiding_spec_4_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec_4_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
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
  // TYPE? qvar |
  //                                   qcon osnl (dot_dot_parens | LEFT_PAREN osnl (cname osnl (COMMA osnl cname)*)? osnl RIGHT_PAREN)?
  public static boolean import_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<import id>");
    r = import_id_0(b, l + 1);
    if (!r) r = import_id_1(b, l + 1);
    exit_section_(b, l, m, HS_IMPORT_ID, r, false, null);
    return r;
  }

  // TYPE? qvar
  private static boolean import_id_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_id_0_0(b, l + 1);
    r = r && qvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TYPE?
  private static boolean import_id_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_0_0")) return false;
    consumeToken(b, HS_TYPE);
    return true;
  }

  // qcon osnl (dot_dot_parens | LEFT_PAREN osnl (cname osnl (COMMA osnl cname)*)? osnl RIGHT_PAREN)?
  private static boolean import_id_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_id_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (dot_dot_parens | LEFT_PAREN osnl (cname osnl (COMMA osnl cname)*)? osnl RIGHT_PAREN)?
  private static boolean import_id_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2")) return false;
    import_id_1_2_0(b, l + 1);
    return true;
  }

  // dot_dot_parens | LEFT_PAREN osnl (cname osnl (COMMA osnl cname)*)? osnl RIGHT_PAREN
  private static boolean import_id_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = dot_dot_parens(b, l + 1);
    if (!r) r = import_id_1_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl (cname osnl (COMMA osnl cname)*)? osnl RIGHT_PAREN
  private static boolean import_id_1_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && import_id_1_2_0_1_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (cname osnl (COMMA osnl cname)*)?
  private static boolean import_id_1_2_0_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0_1_2")) return false;
    import_id_1_2_0_1_2_0(b, l + 1);
    return true;
  }

  // cname osnl (COMMA osnl cname)*
  private static boolean import_id_1_2_0_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_id_1_2_0_1_2_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA osnl cname)*
  private static boolean import_id_1_2_0_1_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0_1_2_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!import_id_1_2_0_1_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_id_1_2_0_1_2_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA osnl cname
  private static boolean import_id_1_2_0_1_2_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_2_0_1_2_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && cname(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PAREN osnl import_id (osnl COMMA osnl import_id)* osnl (COMMA)? osnl RIGHT_PAREN
  public static boolean import_ids_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_ids_spec")) return false;
    if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && import_id(b, l + 1);
    r = r && import_ids_spec_3(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && import_ids_spec_5(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, HS_IMPORT_IDS_SPEC, r);
    return r;
  }

  // (osnl COMMA osnl import_id)*
  private static boolean import_ids_spec_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_ids_spec_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!import_ids_spec_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_ids_spec_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl COMMA osnl import_id
  private static boolean import_ids_spec_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_ids_spec_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && import_id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA)?
  private static boolean import_ids_spec_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_ids_spec_5")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  /* ********************************************************** */
  // mod_id
  public static boolean import_module(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_module")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mod_id(b, l + 1);
    exit_section_(b, m, HS_IMPORT_MODULE, r);
    return r;
  }

  /* ********************************************************** */
  // "qualified"
  public static boolean import_qualified(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_qualified")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<import qualified>");
    r = consumeToken(b, "qualified");
    exit_section_(b, l, m, HS_IMPORT_QUALIFIED, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "as" qualifier
  public static boolean import_qualified_as(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_qualified_as")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<import qualified as>");
    r = consumeToken(b, "as");
    r = r && qualifier(b, l + 1);
    exit_section_(b, l, m, HS_IMPORT_QUALIFIED_AS, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // import_ids_spec |
  //                                   import_hiding_spec |
  //                                   import_empty_spec
  public static boolean import_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_spec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<import spec>");
    r = import_ids_spec(b, l + 1);
    if (!r) r = import_hiding_spec(b, l + 1);
    if (!r) r = import_empty_spec(b, l + 1);
    exit_section_(b, l, m, HS_IMPORT_SPEC, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "INCLUDE" general_pragma_content PRAGMA_END NEWLINE
  public static boolean include_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "INCLUDE");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeTokens(b, 0, HS_PRAGMA_END, HS_NEWLINE);
    exit_section_(b, m, HS_INCLUDE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("INLINABLE" | "INLINEABLE") general_pragma_content PRAGMA_END
  public static boolean inlinable_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inlinable_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && inlinable_pragma_2(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_INLINABLE_PRAGMA, r);
    return r;
  }

  // "INLINABLE" | "INLINEABLE"
  private static boolean inlinable_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inlinable_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "INLINABLE");
    if (!r) r = consumeToken(b, "INLINEABLE");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "INLINE" general_pragma_content PRAGMA_END
  public static boolean inline_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "INLINE");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_INLINE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // qvar gtycon |
  //                                   gtycon? instvar* (LEFT_PAREN osnl gtycon (instvar)* osnl RIGHT_PAREN)+ instvar* |
  //                                   gtycon+ instvar* |
  //                                   qvar instvar* |
  //                                   (LEFT_PAREN osnl instvar (osnl COMMA osnl instvar)+ osnl RIGHT_PAREN)+ instvar* |
  //                                   (LEFT_BRACKET osnl instvar osnl RIGHT_BRACKET)+ |
  //                                   (LEFT_PAREN osnl instvar+ osnl RIGHT_PAREN)+ instvar* |
  //                                   (LEFT_PAREN osnl instvar+ (RIGHT_ARROW osnl instvar* osnl)* osnl RIGHT_PAREN)+ instvar*
  public static boolean inst(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<inst>");
    r = inst_0(b, l + 1);
    if (!r) r = inst_1(b, l + 1);
    if (!r) r = inst_2(b, l + 1);
    if (!r) r = inst_3(b, l + 1);
    if (!r) r = inst_4(b, l + 1);
    if (!r) r = inst_5(b, l + 1);
    if (!r) r = inst_6(b, l + 1);
    if (!r) r = inst_7(b, l + 1);
    exit_section_(b, l, m, HS_INST, r, false, null);
    return r;
  }

  // qvar gtycon
  private static boolean inst_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    r = r && gtycon(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // gtycon? instvar* (LEFT_PAREN osnl gtycon (instvar)* osnl RIGHT_PAREN)+ instvar*
  private static boolean inst_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_1_0(b, l + 1);
    r = r && inst_1_1(b, l + 1);
    r = r && inst_1_2(b, l + 1);
    r = r && inst_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // gtycon?
  private static boolean inst_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_0")) return false;
    gtycon(b, l + 1);
    return true;
  }

  // instvar*
  private static boolean inst_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN osnl gtycon (instvar)* osnl RIGHT_PAREN)+
  private static boolean inst_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_1_2_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl gtycon (instvar)* osnl RIGHT_PAREN
  private static boolean inst_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && gtycon(b, l + 1);
    r = r && inst_1_2_0_3(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (instvar)*
  private static boolean inst_1_2_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_2_0_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!inst_1_2_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_2_0_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (instvar)
  private static boolean inst_1_2_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_2_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // gtycon+ instvar*
  private static boolean inst_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_2_0(b, l + 1);
    r = r && inst_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // gtycon+
  private static boolean inst_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = gtycon(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!gtycon(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_2_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_2_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_2_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // qvar instvar*
  private static boolean inst_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    r = r && inst_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_3_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_3_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN osnl instvar (osnl COMMA osnl instvar)+ osnl RIGHT_PAREN)+ instvar*
  private static boolean inst_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_4_0(b, l + 1);
    r = r && inst_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN osnl instvar (osnl COMMA osnl instvar)+ osnl RIGHT_PAREN)+
  private static boolean inst_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_4_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_4_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl instvar (osnl COMMA osnl instvar)+ osnl RIGHT_PAREN
  private static boolean inst_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && instvar(b, l + 1);
    r = r && inst_4_0_0_3(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl COMMA osnl instvar)+
  private static boolean inst_4_0_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_4_0_0_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_4_0_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_0_0_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // osnl COMMA osnl instvar
  private static boolean inst_4_0_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && instvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_BRACKET osnl instvar osnl RIGHT_BRACKET)+
  private static boolean inst_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_5_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_5", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACKET osnl instvar osnl RIGHT_BRACKET
  private static boolean inst_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && osnl(b, l + 1);
    r = r && instvar(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN osnl instvar+ osnl RIGHT_PAREN)+ instvar*
  private static boolean inst_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_6_0(b, l + 1);
    r = r && inst_6_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN osnl instvar+ osnl RIGHT_PAREN)+
  private static boolean inst_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_6_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_6_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_6_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl instvar+ osnl RIGHT_PAREN
  private static boolean inst_6_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_6_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && inst_6_0_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar+
  private static boolean inst_6_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_6_0_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_6_0_0_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_6_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_6_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_6_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN osnl instvar+ (RIGHT_ARROW osnl instvar* osnl)* osnl RIGHT_PAREN)+ instvar*
  private static boolean inst_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_7_0(b, l + 1);
    r = r && inst_7_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN osnl instvar+ (RIGHT_ARROW osnl instvar* osnl)* osnl RIGHT_PAREN)+
  private static boolean inst_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_7_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_7_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_7_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN osnl instvar+ (RIGHT_ARROW osnl instvar* osnl)* osnl RIGHT_PAREN
  private static boolean inst_7_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && inst_7_0_0_2(b, l + 1);
    r = r && inst_7_0_0_3(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar+
  private static boolean inst_7_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_7_0_0_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (RIGHT_ARROW osnl instvar* osnl)*
  private static boolean inst_7_0_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0_0_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!inst_7_0_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_7_0_0_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // RIGHT_ARROW osnl instvar* osnl
  private static boolean inst_7_0_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_RIGHT_ARROW);
    r = r && osnl(b, l + 1);
    r = r && inst_7_0_0_3_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_7_0_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_0_0_3_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_7_0_0_3_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // instvar*
  private static boolean inst_7_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_7_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_7_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // INSTANCE osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl inst osnl WHERE idecls
  //                                   | INSTANCE osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl inst
  public static boolean instance_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration")) return false;
    if (!nextTokenIs(b, HS_INSTANCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instance_declaration_0(b, l + 1);
    if (!r) r = instance_declaration_1(b, l + 1);
    exit_section_(b, m, HS_INSTANCE_DECLARATION, r);
    return r;
  }

  // INSTANCE osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl inst osnl WHERE idecls
  private static boolean instance_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_INSTANCE);
    r = r && osnl(b, l + 1);
    r = r && instance_declaration_0_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && inst(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_WHERE);
    r = r && idecls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext osnl DOUBLE_RIGHT_ARROW)?
  private static boolean instance_declaration_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_0_2")) return false;
    instance_declaration_0_2_0(b, l + 1);
    return true;
  }

  // scontext osnl DOUBLE_RIGHT_ARROW
  private static boolean instance_declaration_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // INSTANCE osnl (scontext osnl DOUBLE_RIGHT_ARROW)? osnl qcon osnl inst
  private static boolean instance_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_INSTANCE);
    r = r && osnl(b, l + 1);
    r = r && instance_declaration_1_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && inst(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext osnl DOUBLE_RIGHT_ARROW)?
  private static boolean instance_declaration_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_1_2")) return false;
    instance_declaration_1_2_0(b, l + 1);
    return true;
  }

  // scontext osnl DOUBLE_RIGHT_ARROW
  private static boolean instance_declaration_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon | qvar | gcon_sym | LEFT_BRACKET qvar RIGHT_BRACKET | LEFT_PAREN qvar? (gcon_sym | qcon) qvar? RIGHT_PAREN |
  //                                   LEFT_PAREN RIGHT_PAREN
  public static boolean instvar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<instvar>");
    r = qcon(b, l + 1);
    if (!r) r = qvar(b, l + 1);
    if (!r) r = gcon_sym(b, l + 1);
    if (!r) r = instvar_3(b, l + 1);
    if (!r) r = instvar_4(b, l + 1);
    if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
    exit_section_(b, l, m, HS_INSTVAR, r, false, null);
    return r;
  }

  // LEFT_BRACKET qvar RIGHT_BRACKET
  private static boolean instvar_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && qvar(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN qvar? (gcon_sym | qcon) qvar? RIGHT_PAREN
  private static boolean instvar_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && instvar_4_1(b, l + 1);
    r = r && instvar_4_2(b, l + 1);
    r = r && instvar_4_3(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar?
  private static boolean instvar_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_4_1")) return false;
    qvar(b, l + 1);
    return true;
  }

  // gcon_sym | qcon
  private static boolean instvar_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = gcon_sym(b, l + 1);
    if (!r) r = qcon(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar?
  private static boolean instvar_4_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_4_3")) return false;
    qvar(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "LANGUAGE" onl qcon (onl COMMA onl qcon)* onl PRAGMA_END NEWLINE
  public static boolean language_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "LANGUAGE");
    r = r && onl(b, l + 1);
    r = r && qcon(b, l + 1);
    r = r && language_pragma_5(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeTokens(b, 0, HS_PRAGMA_END, HS_NEWLINE);
    exit_section_(b, m, HS_LANGUAGE_PRAGMA, r);
    return r;
  }

  // (onl COMMA onl qcon)*
  private static boolean language_pragma_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!language_pragma_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "language_pragma_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA onl qcon
  private static boolean language_pragma_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && onl(b, l + 1);
    r = r && qcon(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // general_id+ osnl
  public static boolean last_line_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "last_line_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<last line expression>");
    r = last_line_expression_0(b, l + 1);
    r = r && osnl(b, l + 1);
    exit_section_(b, l, m, HS_LAST_LINE_EXPRESSION, r, false, null);
    return r;
  }

  // general_id+
  private static boolean last_line_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "last_line_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "last_line_expression_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NEWLINE* general_id+ snl
  public static boolean line_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<line expression>");
    r = line_expression_0(b, l + 1);
    r = r && line_expression_1(b, l + 1);
    r = r && snl(b, l + 1);
    exit_section_(b, l, m, HS_LINE_EXPRESSION, r, false, null);
    return r;
  }

  // NEWLINE*
  private static boolean line_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, HS_NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "line_expression_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // general_id+
  private static boolean line_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "line_expression_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "LINE" general_pragma_content PRAGMA_END
  public static boolean line_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "LINE");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_LINE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL | HEXADECIMAL | OCTAL | FLOAT | CHARACTER_LITERAL | STRING_LITERAL
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<literal>");
    r = consumeToken(b, HS_DECIMAL);
    if (!r) r = consumeToken(b, HS_HEXADECIMAL);
    if (!r) r = consumeToken(b, HS_OCTAL);
    if (!r) r = consumeToken(b, HS_FLOAT);
    if (!r) r = consumeToken(b, HS_CHARACTER_LITERAL);
    if (!r) r = consumeToken(b, HS_STRING_LITERAL);
    exit_section_(b, l, m, HS_LITERAL, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "MINIMAL" general_pragma_content PRAGMA_END
  public static boolean minimal_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "minimal_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "MINIMAL");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_MINIMAL_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // (CONID_ID DOT)+ CONID_ID | CONID_ID
  public static boolean mod_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mod_id")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mod_id_0(b, l + 1);
    if (!r) r = consumeToken(b, HS_CONID_ID);
    exit_section_(b, m, HS_MOD_ID, r);
    return r;
  }

  // (CONID_ID DOT)+ CONID_ID
  private static boolean mod_id_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mod_id_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mod_id_0_0(b, l + 1);
    r = r && consumeToken(b, HS_CONID_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // (CONID_ID DOT)+
  private static boolean mod_id_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mod_id_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mod_id_0_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!mod_id_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mod_id_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // CONID_ID DOT
  private static boolean mod_id_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mod_id_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CONID_ID, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // module_declaration+ onl body | body
  public static boolean module_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<module body>");
    r = module_body_0(b, l + 1);
    if (!r) r = body(b, l + 1);
    exit_section_(b, l, m, HS_MODULE_BODY, r, false, null);
    return r;
  }

  // module_declaration+ onl body
  private static boolean module_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = module_body_0_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && body(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // module_declaration+
  private static boolean module_body_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_body_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = module_declaration(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!module_declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "module_body_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MODULE mod_id onl deprecated_warn_pragma? onl WHERE? onl (onl exports)? onl WHERE? onl
  public static boolean module_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration")) return false;
    if (!nextTokenIs(b, HS_MODULE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_MODULE);
    r = r && mod_id(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_declaration_3(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_declaration_5(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_declaration_7(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_declaration_9(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, HS_MODULE_DECLARATION, r);
    return r;
  }

  // deprecated_warn_pragma?
  private static boolean module_declaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_3")) return false;
    deprecated_warn_pragma(b, l + 1);
    return true;
  }

  // WHERE?
  private static boolean module_declaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_5")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  // (onl exports)?
  private static boolean module_declaration_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_7")) return false;
    module_declaration_7_0(b, l + 1);
    return true;
  }

  // onl exports
  private static boolean module_declaration_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && exports(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WHERE?
  private static boolean module_declaration_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_9")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  /* ********************************************************** */
  // qcon atype | newconstr_fielddecl
  public static boolean newconstr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<newconstr>");
    r = newconstr_0(b, l + 1);
    if (!r) r = newconstr_fielddecl(b, l + 1);
    exit_section_(b, l, m, HS_NEWCONSTR, r, false, null);
    return r;
  }

  // qcon atype
  private static boolean newconstr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && atype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon osnl LEFT_BRACE? osnl qvar osnl COLON_COLON osnl ttype osnl RIGHT_BRACE?
  public static boolean newconstr_fielddecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_fielddecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<newconstr fielddecl>");
    r = qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && newconstr_fielddecl_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && qvar(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_COLON_COLON);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && newconstr_fielddecl_10(b, l + 1);
    exit_section_(b, l, m, HS_NEWCONSTR_FIELDDECL, r, false, null);
    return r;
  }

  // LEFT_BRACE?
  private static boolean newconstr_fielddecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_fielddecl_2")) return false;
    consumeToken(b, HS_LEFT_BRACE);
    return true;
  }

  // RIGHT_BRACE?
  private static boolean newconstr_fielddecl_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_fielddecl_10")) return false;
    consumeToken(b, HS_RIGHT_BRACE);
    return true;
  }

  /* ********************************************************** */
  // NEWTYPE osnl ctype_pragma? osnl (context osnl DOUBLE_RIGHT_ARROW)? osnl simpletype osnl EQUAL osnl newconstr osnl (DERIVING osnl ttype)?
  public static boolean newtype_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration")) return false;
    if (!nextTokenIs(b, HS_NEWTYPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_NEWTYPE);
    r = r && osnl(b, l + 1);
    r = r && newtype_declaration_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && newtype_declaration_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_EQUAL);
    r = r && osnl(b, l + 1);
    r = r && newconstr(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && newtype_declaration_12(b, l + 1);
    exit_section_(b, m, HS_NEWTYPE_DECLARATION, r);
    return r;
  }

  // ctype_pragma?
  private static boolean newtype_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_2")) return false;
    ctype_pragma(b, l + 1);
    return true;
  }

  // (context osnl DOUBLE_RIGHT_ARROW)?
  private static boolean newtype_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_4")) return false;
    newtype_declaration_4_0(b, l + 1);
    return true;
  }

  // context osnl DOUBLE_RIGHT_ARROW
  private static boolean newtype_declaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = context(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DERIVING osnl ttype)?
  private static boolean newtype_declaration_12(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_12")) return false;
    newtype_declaration_12_0(b, l + 1);
    return true;
  }

  // DERIVING osnl ttype
  private static boolean newtype_declaration_12_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_12_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DERIVING);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("NOINLINE" | "NOTINLINE") general_id+ PRAGMA_END
  public static boolean noinline_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "noinline_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && noinline_pragma_2(b, l + 1);
    r = r && noinline_pragma_3(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_NOINLINE_PRAGMA, r);
    return r;
  }

  // "NOINLINE" | "NOTINLINE"
  private static boolean noinline_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "noinline_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "NOINLINE");
    if (!r) r = consumeToken(b, "NOTINLINE");
    exit_section_(b, m, null, r);
    return r;
  }

  // general_id+
  private static boolean noinline_pragma_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "noinline_pragma_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "noinline_pragma_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "NOUNPACK" onl PRAGMA_END
  public static boolean nounpack_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nounpack_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "NOUNPACK");
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_NOUNPACK_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // NEWLINE*
  static boolean onl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onl")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, HS_NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "onl", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // qvar_op | qcon_op
  public static boolean op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "op")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<op>");
    r = qvar_op(b, l + 1);
    if (!r) r = qcon_op(b, l + 1);
    exit_section_(b, l, m, HS_OP, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // op (COMMA op)*
  public static boolean ops(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ops")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<ops>");
    r = op(b, l + 1);
    r = r && ops_1(b, l + 1);
    exit_section_(b, l, m, HS_OPS, r, false, null);
    return r;
  }

  // (COMMA op)*
  private static boolean ops_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ops_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ops_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ops_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA op
  private static boolean ops_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ops_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && op(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("OPTIONS_GHC" | "OPTIONS") general_pragma_content PRAGMA_END NEWLINE
  public static boolean options_ghc_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && options_ghc_pragma_2(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeTokens(b, 0, HS_PRAGMA_END, HS_NEWLINE);
    exit_section_(b, m, HS_OPTIONS_GHC_PRAGMA, r);
    return r;
  }

  // "OPTIONS_GHC" | "OPTIONS"
  private static boolean options_ghc_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "OPTIONS_GHC");
    if (!r) r = consumeToken(b, "OPTIONS");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (LEFT_BRACE? RIGHT_BRACE? SEMICOLON? BACKSLASH? &<<containsSpaces>> NEWLINE)*
  static boolean osnl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl")) return false;
    int c = current_position_(b);
    while (true) {
      if (!osnl_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "osnl", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_BRACE? RIGHT_BRACE? SEMICOLON? BACKSLASH? &<<containsSpaces>> NEWLINE
  private static boolean osnl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl_0_0(b, l + 1);
    r = r && osnl_0_1(b, l + 1);
    r = r && osnl_0_2(b, l + 1);
    r = r && osnl_0_3(b, l + 1);
    r = r && osnl_0_4(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE?
  private static boolean osnl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0_0")) return false;
    consumeToken(b, HS_LEFT_BRACE);
    return true;
  }

  // RIGHT_BRACE?
  private static boolean osnl_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0_1")) return false;
    consumeToken(b, HS_RIGHT_BRACE);
    return true;
  }

  // SEMICOLON?
  private static boolean osnl_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0_2")) return false;
    consumeToken(b, HS_SEMICOLON);
    return true;
  }

  // BACKSLASH?
  private static boolean osnl_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0_3")) return false;
    consumeToken(b, HS_BACKSLASH);
    return true;
  }

  // &<<containsSpaces>>
  private static boolean osnl_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "osnl_0_4")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_, null);
    r = containsSpaces(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ann_pragma | deprecated_warn_pragma | noinline_pragma | inlinable_pragma | line_pragma | rules_pragma |
  //                                   specialize_pragma | inline_pragma | minimal_pragma
  public static boolean other_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ann_pragma(b, l + 1);
    if (!r) r = deprecated_warn_pragma(b, l + 1);
    if (!r) r = noinline_pragma(b, l + 1);
    if (!r) r = inlinable_pragma(b, l + 1);
    if (!r) r = line_pragma(b, l + 1);
    if (!r) r = rules_pragma(b, l + 1);
    if (!r) r = specialize_pragma(b, l + 1);
    if (!r) r = inline_pragma(b, l + 1);
    if (!r) r = minimal_pragma(b, l + 1);
    exit_section_(b, m, HS_OTHER_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET COLON_COLON RIGHT_BRACKET
  public static boolean parallel_array_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parallel_array_type")) return false;
    if (!nextTokenIs(b, HS_LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_LEFT_BRACKET, HS_COLON_COLON, HS_RIGHT_BRACKET);
    exit_section_(b, m, HS_PARALLEL_ARRAY_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // onl file_header? onl module_body
  static boolean program(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && program_1(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_body(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // file_header?
  private static boolean program_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program_1")) return false;
    file_header(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // qcon_id | LEFT_PAREN gcon_sym RIGHT_PAREN | con_id | LEFT_PAREN con_sym RIGHT_PAREN
  public static boolean qcon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<qcon>");
    r = qcon_id(b, l + 1);
    if (!r) r = qcon_1(b, l + 1);
    if (!r) r = con_id(b, l + 1);
    if (!r) r = qcon_3(b, l + 1);
    exit_section_(b, l, m, HS_QCON, r, false, null);
    return r;
  }

  // LEFT_PAREN gcon_sym RIGHT_PAREN
  private static boolean qcon_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && gcon_sym(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN con_sym RIGHT_PAREN
  private static boolean qcon_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && con_sym(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon_id_qualifier con_id
  public static boolean qcon_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_id")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon_id_qualifier(b, l + 1);
    r = r && con_id(b, l + 1);
    exit_section_(b, m, HS_QCON_ID, r);
    return r;
  }

  /* ********************************************************** */
  // (CONID_ID DOT)+
  public static boolean qcon_id_qualifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_id_qualifier")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon_id_qualifier_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qcon_id_qualifier_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qcon_id_qualifier", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, HS_QCON_ID_QUALIFIER, r);
    return r;
  }

  // CONID_ID DOT
  private static boolean qcon_id_qualifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_id_qualifier_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CONID_ID, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // gcon_sym | BACKQUOTE qcon_id BACKQUOTE | con_sym | BACKQUOTE con_id BACKQUOTE
  public static boolean qcon_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_op")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<qcon op>");
    r = gcon_sym(b, l + 1);
    if (!r) r = qcon_op_1(b, l + 1);
    if (!r) r = con_sym(b, l + 1);
    if (!r) r = qcon_op_3(b, l + 1);
    exit_section_(b, l, m, HS_QCON_OP, r, false, null);
    return r;
  }

  // BACKQUOTE qcon_id BACKQUOTE
  private static boolean qcon_op_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_op_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && qcon_id(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  // BACKQUOTE con_id BACKQUOTE
  private static boolean qcon_op_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_op_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && con_id(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifier DOT con_sym
  public static boolean qcon_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qcon_sym")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && con_sym(b, l + 1);
    exit_section_(b, m, HS_QCON_SYM, r);
    return r;
  }

  /* ********************************************************** */
  // general_id+ NEWLINE? quasi_quote
  public static boolean qq_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qq_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<qq expression>");
    r = qq_expression_0(b, l + 1);
    r = r && qq_expression_1(b, l + 1);
    r = r && quasi_quote(b, l + 1);
    exit_section_(b, l, m, HS_QQ_EXPRESSION, r, false, null);
    return r;
  }

  // general_id+
  private static boolean qq_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qq_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qq_expression_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean qq_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qq_expression_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // (CONID_ID DOT)+ CONID_ID | CONID_ID
  public static boolean qualifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier_0(b, l + 1);
    if (!r) r = consumeToken(b, HS_CONID_ID);
    exit_section_(b, m, HS_QUALIFIER, r);
    return r;
  }

  // (CONID_ID DOT)+ CONID_ID
  private static boolean qualifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier_0_0(b, l + 1);
    r = r && consumeToken(b, HS_CONID_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // (CONID_ID DOT)+
  private static boolean qualifier_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier_0_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qualifier_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qualifier_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // CONID_ID DOT
  private static boolean qualifier_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CONID_ID, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // QUASI_QUOTE_D_START NEWLINE? top_declarations NEWLINE? QUASI_QUOTE_END |
  //                                   QUASI_QUOTE_E_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END |
  //                                   QUASI_QUOTE_T_START NEWLINE? simpletype NEWLINE? QUASI_QUOTE_END |
  //                                   QUASI_QUOTE_P_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END |
  //                                   QUASI_QUOTE_V_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END
  public static boolean quasi_quote(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<quasi quote>");
    r = quasi_quote_0(b, l + 1);
    if (!r) r = quasi_quote_1(b, l + 1);
    if (!r) r = quasi_quote_2(b, l + 1);
    if (!r) r = quasi_quote_3(b, l + 1);
    if (!r) r = quasi_quote_4(b, l + 1);
    exit_section_(b, l, m, HS_QUASI_QUOTE, r, false, null);
    return r;
  }

  // QUASI_QUOTE_D_START NEWLINE? top_declarations NEWLINE? QUASI_QUOTE_END
  private static boolean quasi_quote_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_QUASI_QUOTE_D_START);
    r = r && quasi_quote_0_1(b, l + 1);
    r = r && top_declarations(b, l + 1);
    r = r && quasi_quote_0_3(b, l + 1);
    r = r && consumeToken(b, HS_QUASI_QUOTE_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean quasi_quote_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_0_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // NEWLINE?
  private static boolean quasi_quote_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_0_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // QUASI_QUOTE_E_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END
  private static boolean quasi_quote_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_QUASI_QUOTE_E_START);
    r = r && quasi_quote_1_1(b, l + 1);
    r = r && expression(b, l + 1);
    r = r && quasi_quote_1_3(b, l + 1);
    r = r && consumeToken(b, HS_QUASI_QUOTE_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean quasi_quote_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_1_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // NEWLINE?
  private static boolean quasi_quote_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_1_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // QUASI_QUOTE_T_START NEWLINE? simpletype NEWLINE? QUASI_QUOTE_END
  private static boolean quasi_quote_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_QUASI_QUOTE_T_START);
    r = r && quasi_quote_2_1(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && quasi_quote_2_3(b, l + 1);
    r = r && consumeToken(b, HS_QUASI_QUOTE_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean quasi_quote_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_2_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // NEWLINE?
  private static boolean quasi_quote_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_2_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // QUASI_QUOTE_P_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END
  private static boolean quasi_quote_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_QUASI_QUOTE_P_START);
    r = r && quasi_quote_3_1(b, l + 1);
    r = r && expression(b, l + 1);
    r = r && quasi_quote_3_3(b, l + 1);
    r = r && consumeToken(b, HS_QUASI_QUOTE_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean quasi_quote_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_3_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // NEWLINE?
  private static boolean quasi_quote_3_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_3_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // QUASI_QUOTE_V_START NEWLINE? expression NEWLINE? QUASI_QUOTE_END
  private static boolean quasi_quote_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_QUASI_QUOTE_V_START);
    r = r && quasi_quote_4_1(b, l + 1);
    r = r && expression(b, l + 1);
    r = r && quasi_quote_4_3(b, l + 1);
    r = r && consumeToken(b, HS_QUASI_QUOTE_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean quasi_quote_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_4_1")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  // NEWLINE?
  private static boolean quasi_quote_4_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quasi_quote_4_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // qvar_id | LEFT_PAREN (qvar_sym | qvar_dot_sym) RIGHT_PAREN | var_id | LEFT_PAREN (var_sym | var_dot_sym) RIGHT_PAREN
  public static boolean qvar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<qvar>");
    r = qvar_id(b, l + 1);
    if (!r) r = qvar_1(b, l + 1);
    if (!r) r = var_id(b, l + 1);
    if (!r) r = qvar_3(b, l + 1);
    exit_section_(b, l, m, HS_QVAR, r, false, null);
    return r;
  }

  // LEFT_PAREN (qvar_sym | qvar_dot_sym) RIGHT_PAREN
  private static boolean qvar_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && qvar_1_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar_sym | qvar_dot_sym
  private static boolean qvar_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar_sym(b, l + 1);
    if (!r) r = qvar_dot_sym(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN (var_sym | var_dot_sym) RIGHT_PAREN
  private static boolean qvar_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && qvar_3_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // var_sym | var_dot_sym
  private static boolean qvar_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = var_sym(b, l + 1);
    if (!r) r = var_dot_sym(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifier DOT var_dot_sym
  public static boolean qvar_dot_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_dot_sym")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && var_dot_sym(b, l + 1);
    exit_section_(b, m, HS_QVAR_DOT_SYM, r);
    return r;
  }

  /* ********************************************************** */
  // qualifier DOT var_id
  public static boolean qvar_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_id")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && var_id(b, l + 1);
    exit_section_(b, m, HS_QVAR_ID, r);
    return r;
  }

  /* ********************************************************** */
  // qvar_sym | qvar_dot_sym | BACKQUOTE qvar_id BACKQUOTE | var_sym | var_dot_sym | BACKQUOTE var_id BACKQUOTE
  public static boolean qvar_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_op")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<qvar op>");
    r = qvar_sym(b, l + 1);
    if (!r) r = qvar_dot_sym(b, l + 1);
    if (!r) r = qvar_op_2(b, l + 1);
    if (!r) r = var_sym(b, l + 1);
    if (!r) r = var_dot_sym(b, l + 1);
    if (!r) r = qvar_op_5(b, l + 1);
    exit_section_(b, l, m, HS_QVAR_OP, r, false, null);
    return r;
  }

  // BACKQUOTE qvar_id BACKQUOTE
  private static boolean qvar_op_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_op_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && qvar_id(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  // BACKQUOTE var_id BACKQUOTE
  private static boolean qvar_op_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_op_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && var_id(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifier DOT var_sym
  public static boolean qvar_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qvar_sym")) return false;
    if (!nextTokenIs(b, HS_CONID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && var_sym(b, l + 1);
    exit_section_(b, m, HS_QVAR_SYM, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "RULES" general_pragma_content PRAGMA_END
  public static boolean rules_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rules_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "RULES");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_RULES_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // simpleclass? LEFT_PAREN osnl simpleclass osnl (osnl COMMA osnl simpleclass)* osnl RIGHT_PAREN |
  //                                   simpleclass
  public static boolean scontext(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scontext")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<scontext>");
    r = scontext_0(b, l + 1);
    if (!r) r = simpleclass(b, l + 1);
    exit_section_(b, l, m, HS_SCONTEXT, r, false, null);
    return r;
  }

  // simpleclass? LEFT_PAREN osnl simpleclass osnl (osnl COMMA osnl simpleclass)* osnl RIGHT_PAREN
  private static boolean scontext_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scontext_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext_0_0(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && osnl(b, l + 1);
    r = r && simpleclass(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && scontext_0_5(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // simpleclass?
  private static boolean scontext_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scontext_0_0")) return false;
    simpleclass(b, l + 1);
    return true;
  }

  // (osnl COMMA osnl simpleclass)*
  private static boolean scontext_0_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scontext_0_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!scontext_0_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scontext_0_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // osnl COMMA osnl simpleclass
  private static boolean scontext_0_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scontext_0_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && osnl(b, l + 1);
    r = r && simpleclass(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // simpleclass_tilde_part TILDE simpleclass_tilde_part |
  //                                   LEFT_PAREN simpleclass_tilde_part TILDE simpleclass_tilde_part RIGHT_PAREN |
  //                                   qcon+ qvar+ |
  //                                   atype
  public static boolean simpleclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<simpleclass>");
    r = simpleclass_0(b, l + 1);
    if (!r) r = simpleclass_1(b, l + 1);
    if (!r) r = simpleclass_2(b, l + 1);
    if (!r) r = atype(b, l + 1);
    exit_section_(b, l, m, HS_SIMPLECLASS, r, false, null);
    return r;
  }

  // simpleclass_tilde_part TILDE simpleclass_tilde_part
  private static boolean simpleclass_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleclass_tilde_part(b, l + 1);
    r = r && consumeToken(b, HS_TILDE);
    r = r && simpleclass_tilde_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN simpleclass_tilde_part TILDE simpleclass_tilde_part RIGHT_PAREN
  private static boolean simpleclass_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && simpleclass_tilde_part(b, l + 1);
    r = r && consumeToken(b, HS_TILDE);
    r = r && simpleclass_tilde_part(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon+ qvar+
  private static boolean simpleclass_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleclass_2_0(b, l + 1);
    r = r && simpleclass_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon+
  private static boolean simpleclass_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qcon(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_2_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar+
  private static boolean simpleclass_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_2_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (qcon LEFT_PAREN qcon qvar RIGHT_PAREN)? qvar+ | qcon qvar*
  public static boolean simpleclass_tilde_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<simpleclass tilde part>");
    r = simpleclass_tilde_part_0(b, l + 1);
    if (!r) r = simpleclass_tilde_part_1(b, l + 1);
    exit_section_(b, l, m, HS_SIMPLECLASS_TILDE_PART, r, false, null);
    return r;
  }

  // (qcon LEFT_PAREN qcon qvar RIGHT_PAREN)? qvar+
  private static boolean simpleclass_tilde_part_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleclass_tilde_part_0_0(b, l + 1);
    r = r && simpleclass_tilde_part_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (qcon LEFT_PAREN qcon qvar RIGHT_PAREN)?
  private static boolean simpleclass_tilde_part_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_0_0")) return false;
    simpleclass_tilde_part_0_0_0(b, l + 1);
    return true;
  }

  // qcon LEFT_PAREN qcon qvar RIGHT_PAREN
  private static boolean simpleclass_tilde_part_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && qcon(b, l + 1);
    r = r && qvar(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar+
  private static boolean simpleclass_tilde_part_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_tilde_part_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon qvar*
  private static boolean simpleclass_tilde_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && simpleclass_tilde_part_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar*
  private static boolean simpleclass_tilde_part_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_tilde_part_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_tilde_part_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // parallel_array_type qvar  |
  //                                   qcon? ttype |
  //                                   qcon osnl qvar* osnl (LEFT_PAREN type_signature_declaration RIGHT_PAREN)+ osnl qvar* |
  //                                   (qcon | qvar)+ |
  //                                   qvar* osnl (LEFT_PAREN qvar_op RIGHT_PAREN | LEFT_PAREN gcon_sym RIGHT_PAREN) osnl qvar*
  public static boolean simpletype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<simpletype>");
    r = simpletype_0(b, l + 1);
    if (!r) r = simpletype_1(b, l + 1);
    if (!r) r = simpletype_2(b, l + 1);
    if (!r) r = simpletype_3(b, l + 1);
    if (!r) r = simpletype_4(b, l + 1);
    exit_section_(b, l, m, HS_SIMPLETYPE, r, false, null);
    return r;
  }

  // parallel_array_type qvar
  private static boolean simpletype_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parallel_array_type(b, l + 1);
    r = r && qvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon? ttype
  private static boolean simpletype_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_1_0(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon?
  private static boolean simpletype_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_1_0")) return false;
    qcon(b, l + 1);
    return true;
  }

  // qcon osnl qvar* osnl (LEFT_PAREN type_signature_declaration RIGHT_PAREN)+ osnl qvar*
  private static boolean simpletype_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype_2_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype_2_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype_2_6(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar*
  private static boolean simpletype_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_2_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN type_signature_declaration RIGHT_PAREN)+
  private static boolean simpletype_2_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_2_4_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!simpletype_2_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_2_4", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN type_signature_declaration RIGHT_PAREN
  private static boolean simpletype_2_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && type_signature_declaration(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar*
  private static boolean simpletype_2_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_2_6", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (qcon | qvar)+
  private static boolean simpletype_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!simpletype_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // qcon | qvar
  private static boolean simpletype_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qcon(b, l + 1);
    if (!r) r = qvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar* osnl (LEFT_PAREN qvar_op RIGHT_PAREN | LEFT_PAREN gcon_sym RIGHT_PAREN) osnl qvar*
  private static boolean simpletype_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_4_0(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype_4_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && simpletype_4_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar*
  private static boolean simpletype_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_4_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_PAREN qvar_op RIGHT_PAREN | LEFT_PAREN gcon_sym RIGHT_PAREN
  private static boolean simpletype_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_4_2_0(b, l + 1);
    if (!r) r = simpletype_4_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN qvar_op RIGHT_PAREN
  private static boolean simpletype_4_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && qvar_op(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN gcon_sym RIGHT_PAREN
  private static boolean simpletype_4_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && gcon_sym(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // qvar*
  private static boolean simpletype_4_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_4_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_4_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // (LEFT_BRACE? RIGHT_BRACE? SEMICOLON? BACKSLASH? &<<containsSpaces>> NEWLINE)+
  public static boolean snl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<snl>");
    r = snl_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!snl_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "snl", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, HS_SNL, r, false, null);
    return r;
  }

  // LEFT_BRACE? RIGHT_BRACE? SEMICOLON? BACKSLASH? &<<containsSpaces>> NEWLINE
  private static boolean snl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = snl_0_0(b, l + 1);
    r = r && snl_0_1(b, l + 1);
    r = r && snl_0_2(b, l + 1);
    r = r && snl_0_3(b, l + 1);
    r = r && snl_0_4(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE?
  private static boolean snl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0_0")) return false;
    consumeToken(b, HS_LEFT_BRACE);
    return true;
  }

  // RIGHT_BRACE?
  private static boolean snl_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0_1")) return false;
    consumeToken(b, HS_RIGHT_BRACE);
    return true;
  }

  // SEMICOLON?
  private static boolean snl_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0_2")) return false;
    consumeToken(b, HS_SEMICOLON);
    return true;
  }

  // BACKSLASH?
  private static boolean snl_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0_3")) return false;
    consumeToken(b, HS_BACKSLASH);
    return true;
  }

  // &<<containsSpaces>>
  private static boolean snl_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "snl_0_4")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_, null);
    r = containsSpaces(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "SOURCE" onl PRAGMA_END
  public static boolean source_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "source_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "SOURCE");
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_SOURCE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("SPECIALIZE" | "SPECIALISE") general_pragma_content PRAGMA_END
  public static boolean specialize_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "specialize_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && specialize_pragma_2(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_SPECIALIZE_PRAGMA, r);
    return r;
  }

  // "SPECIALIZE" | "SPECIALISE"
  private static boolean specialize_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "specialize_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "SPECIALIZE");
    if (!r) r = consumeToken(b, "SPECIALISE");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // btype | qvar_op atype
  public static boolean sub_constr2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sub_constr2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<sub constr 2>");
    r = btype(b, l + 1);
    if (!r) r = sub_constr2_1(b, l + 1);
    exit_section_(b, l, m, HS_SUB_CONSTR_2, r, false, null);
    return r;
  }

  // qvar_op atype
  private static boolean sub_constr2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sub_constr2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qvar_op(b, l + 1);
    r = r && atype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AT | BACKSLASH | VERTICAL_BAR | TILDE | EQUAL
  static boolean symbol_reserved_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_reserved_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_AT);
    if (!r) r = consumeToken(b, HS_BACKSLASH);
    if (!r) r = consumeToken(b, HS_VERTICAL_BAR);
    if (!r) r = consumeToken(b, HS_TILDE);
    if (!r) r = consumeToken(b, HS_EQUAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // type_declaration | data_declaration | newtype_declaration | class_declaration | instance_declaration | default_declaration |
  //                                   foreign_declaration | type_family_declaration | deriving_declaration | type_instance_declaration | type_signature_declaration |
  //                                   other_pragma | quasi_quote | qq_expression | expression | cfiles_pragma
  public static boolean top_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<top declaration>");
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
    if (!r) r = type_signature_declaration(b, l + 1);
    if (!r) r = other_pragma(b, l + 1);
    if (!r) r = quasi_quote(b, l + 1);
    if (!r) r = qq_expression(b, l + 1);
    if (!r) r = expression(b, l + 1);
    if (!r) r = cfiles_pragma(b, l + 1);
    exit_section_(b, l, m, HS_TOP_DECLARATION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (top_declaration onl)*
  static boolean top_declarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations")) return false;
    int c = current_position_(b);
    while (true) {
      if (!top_declarations_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "top_declarations", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // top_declaration onl
  private static boolean top_declarations_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = top_declaration(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // btype (osnl RIGHT_ARROW osnl ttype)? | parallel_array_type
  public static boolean ttype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<ttype>");
    r = ttype_0(b, l + 1);
    if (!r) r = parallel_array_type(b, l + 1);
    exit_section_(b, l, m, HS_TTYPE, r, false, null);
    return r;
  }

  // btype (osnl RIGHT_ARROW osnl ttype)?
  private static boolean ttype_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = btype(b, l + 1);
    r = r && ttype_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (osnl RIGHT_ARROW osnl ttype)?
  private static boolean ttype_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_0_1")) return false;
    ttype_0_1_0(b, l + 1);
    return true;
  }

  // osnl RIGHT_ARROW osnl ttype
  private static boolean ttype_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = osnl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_ARROW);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TYPE osnl simpletype osnl COLON_COLON osnl ttype |
  //                                   TYPE osnl simpletype osnl (EQUAL | WHERE)? osnl (ttype | type_signature_declaration)? (DOUBLE_RIGHT_ARROW ttype)? |
  //                                   TYPE osnl simpletype
  public static boolean type_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration")) return false;
    if (!nextTokenIs(b, HS_TYPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_declaration_0(b, l + 1);
    if (!r) r = type_declaration_1(b, l + 1);
    if (!r) r = type_declaration_2(b, l + 1);
    exit_section_(b, m, HS_TYPE_DECLARATION, r);
    return r;
  }

  // TYPE osnl simpletype osnl COLON_COLON osnl ttype
  private static boolean type_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_COLON_COLON);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TYPE osnl simpletype osnl (EQUAL | WHERE)? osnl (ttype | type_signature_declaration)? (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean type_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_declaration_1_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_declaration_1_6(b, l + 1);
    r = r && type_declaration_1_7(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (EQUAL | WHERE)?
  private static boolean type_declaration_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_4")) return false;
    type_declaration_1_4_0(b, l + 1);
    return true;
  }

  // EQUAL | WHERE
  private static boolean type_declaration_1_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_EQUAL);
    if (!r) r = consumeToken(b, HS_WHERE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ttype | type_signature_declaration)?
  private static boolean type_declaration_1_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_6")) return false;
    type_declaration_1_6_0(b, l + 1);
    return true;
  }

  // ttype | type_signature_declaration
  private static boolean type_declaration_1_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    if (!r) r = type_signature_declaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean type_declaration_1_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_7")) return false;
    type_declaration_1_7_0(b, l + 1);
    return true;
  }

  // DOUBLE_RIGHT_ARROW ttype
  private static boolean type_declaration_1_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TYPE osnl simpletype
  private static boolean type_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && osnl(b, l + 1);
    r = r && simpletype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TYPE_FAMILY osnl type_family_type osnl WHERE? osnl expression?
  public static boolean type_family_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_declaration")) return false;
    if (!nextTokenIs(b, HS_TYPE_FAMILY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE_FAMILY);
    r = r && osnl(b, l + 1);
    r = r && type_family_type(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_family_declaration_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_family_declaration_6(b, l + 1);
    exit_section_(b, m, HS_TYPE_FAMILY_DECLARATION, r);
    return r;
  }

  // WHERE?
  private static boolean type_family_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_declaration_4")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  // expression?
  private static boolean type_family_declaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_declaration_6")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (type_family_type1 | type_family_type2 | LEFT_PAREN? vars COLON_COLON? osnl (context DOUBLE_RIGHT_ARROW)? osnl ttype? RIGHT_PAREN?)+ (COLON_COLON osnl ttype)?
  public static boolean type_family_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type family type>");
    r = type_family_type_0(b, l + 1);
    r = r && type_family_type_1(b, l + 1);
    exit_section_(b, l, m, HS_TYPE_FAMILY_TYPE, r, false, null);
    return r;
  }

  // (type_family_type1 | type_family_type2 | LEFT_PAREN? vars COLON_COLON? osnl (context DOUBLE_RIGHT_ARROW)? osnl ttype? RIGHT_PAREN?)+
  private static boolean type_family_type_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_family_type_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!type_family_type_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "type_family_type_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // type_family_type1 | type_family_type2 | LEFT_PAREN? vars COLON_COLON? osnl (context DOUBLE_RIGHT_ARROW)? osnl ttype? RIGHT_PAREN?
  private static boolean type_family_type_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_family_type1(b, l + 1);
    if (!r) r = type_family_type2(b, l + 1);
    if (!r) r = type_family_type_0_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN? vars COLON_COLON? osnl (context DOUBLE_RIGHT_ARROW)? osnl ttype? RIGHT_PAREN?
  private static boolean type_family_type_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_family_type_0_0_2_0(b, l + 1);
    r = r && vars(b, l + 1);
    r = r && type_family_type_0_0_2_2(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_family_type_0_0_2_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && type_family_type_0_0_2_6(b, l + 1);
    r = r && type_family_type_0_0_2_7(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN?
  private static boolean type_family_type_0_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_0")) return false;
    consumeToken(b, HS_LEFT_PAREN);
    return true;
  }

  // COLON_COLON?
  private static boolean type_family_type_0_0_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_2")) return false;
    consumeToken(b, HS_COLON_COLON);
    return true;
  }

  // (context DOUBLE_RIGHT_ARROW)?
  private static boolean type_family_type_0_0_2_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_4")) return false;
    type_family_type_0_0_2_4_0(b, l + 1);
    return true;
  }

  // context DOUBLE_RIGHT_ARROW
  private static boolean type_family_type_0_0_2_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = context(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype?
  private static boolean type_family_type_0_0_2_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_6")) return false;
    ttype(b, l + 1);
    return true;
  }

  // RIGHT_PAREN?
  private static boolean type_family_type_0_0_2_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_2_7")) return false;
    consumeToken(b, HS_RIGHT_PAREN);
    return true;
  }

  // (COLON_COLON osnl ttype)?
  private static boolean type_family_type_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_1")) return false;
    type_family_type_1_0(b, l + 1);
    return true;
  }

  // COLON_COLON osnl ttype
  private static boolean type_family_type_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COLON_COLON);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qcon
  public static boolean type_family_type1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type family type 1>");
    r = qcon(b, l + 1);
    exit_section_(b, l, m, HS_TYPE_FAMILY_TYPE_1, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // qvar_op
  public static boolean type_family_type2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type family type 2>");
    r = qvar_op(b, l + 1);
    exit_section_(b, l, m, HS_TYPE_FAMILY_TYPE_2, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TYPE_INSTANCE osnl expression
  public static boolean type_instance_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_instance_declaration")) return false;
    if (!nextTokenIs(b, HS_TYPE_INSTANCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE_INSTANCE);
    r = r && osnl(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, m, HS_TYPE_INSTANCE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // vars osnl COLON_COLON osnl ((ttype | context) DOUBLE_RIGHT_ARROW)? osnl ttype | fixity (DECIMAL)? ops
  public static boolean type_signature_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type signature declaration>");
    r = type_signature_declaration_0(b, l + 1);
    if (!r) r = type_signature_declaration_1(b, l + 1);
    exit_section_(b, l, m, HS_TYPE_SIGNATURE_DECLARATION, r, false, null);
    return r;
  }

  // vars osnl COLON_COLON osnl ((ttype | context) DOUBLE_RIGHT_ARROW)? osnl ttype
  private static boolean type_signature_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = vars(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && consumeToken(b, HS_COLON_COLON);
    r = r && osnl(b, l + 1);
    r = r && type_signature_declaration_0_4(b, l + 1);
    r = r && osnl(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ((ttype | context) DOUBLE_RIGHT_ARROW)?
  private static boolean type_signature_declaration_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_0_4")) return false;
    type_signature_declaration_0_4_0(b, l + 1);
    return true;
  }

  // (ttype | context) DOUBLE_RIGHT_ARROW
  private static boolean type_signature_declaration_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_signature_declaration_0_4_0_0(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype | context
  private static boolean type_signature_declaration_0_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_0_4_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    if (!r) r = context(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // fixity (DECIMAL)? ops
  private static boolean type_signature_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fixity(b, l + 1);
    r = r && type_signature_declaration_1_1(b, l + 1);
    r = r && ops(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DECIMAL)?
  private static boolean type_signature_declaration_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_declaration_1_1")) return false;
    consumeToken(b, HS_DECIMAL);
    return true;
  }

  /* ********************************************************** */
  // unpack_pragma | nounpack_pragma
  public static boolean unpack_nounpack_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unpack_nounpack_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unpack_pragma(b, l + 1);
    if (!r) r = nounpack_pragma(b, l + 1);
    exit_section_(b, m, HS_UNPACK_NOUNPACK_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "UNPACK" onl PRAGMA_END
  public static boolean unpack_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unpack_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "UNPACK");
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_UNPACK_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // DOT DOT | DOT+ VARSYM_ID | DOT
  public static boolean var_dot_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_dot_sym")) return false;
    if (!nextTokenIs(b, HS_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, HS_DOT, HS_DOT);
    if (!r) r = var_dot_sym_1(b, l + 1);
    if (!r) r = consumeToken(b, HS_DOT);
    exit_section_(b, m, HS_VAR_DOT_SYM, r);
    return r;
  }

  // DOT+ VARSYM_ID
  private static boolean var_dot_sym_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_dot_sym_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = var_dot_sym_1_0(b, l + 1);
    r = r && consumeToken(b, HS_VARSYM_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT+
  private static boolean var_dot_sym_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_dot_sym_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOT);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, HS_DOT)) break;
      if (!empty_element_parsed_guard_(b, "var_dot_sym_1_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VARID_ID
  public static boolean var_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_id")) return false;
    if (!nextTokenIs(b, HS_VARID_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VARID_ID);
    exit_section_(b, m, HS_VAR_ID, r);
    return r;
  }

  /* ********************************************************** */
  // VARSYM_ID | TILDE
  public static boolean var_sym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_sym")) return false;
    if (!nextTokenIs(b, "<var sym>", HS_TILDE, HS_VARSYM_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<var sym>");
    r = consumeToken(b, HS_VARSYM_ID);
    if (!r) r = consumeToken(b, HS_TILDE);
    exit_section_(b, l, m, HS_VAR_SYM, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // qvar (COMMA qvar)*
  public static boolean vars(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vars")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<vars>");
    r = qvar(b, l + 1);
    r = r && vars_1(b, l + 1);
    exit_section_(b, l, m, HS_VARS, r, false, null);
    return r;
  }

  // (COMMA qvar)*
  private static boolean vars_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vars_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!vars_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "vars_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA qvar
  private static boolean vars_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vars_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && qvar(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

}
