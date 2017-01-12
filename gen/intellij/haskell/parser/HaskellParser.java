// This is a generated file. Not intended for manual editing.
package intellij.haskell.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

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
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == HS_ANN_PRAGMA) {
      r = ann_pragma(b, 0);
    }
    else if (t == HS_CCONTEXT) {
      r = ccontext(b, 0);
    }
    else if (t == HS_CFILES_PRAGMA) {
      r = cfiles_pragma(b, 0);
    }
    else if (t == HS_CIDECLS) {
      r = cidecls(b, 0);
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
    else if (t == HS_CON) {
      r = con(b, 0);
    }
    else if (t == HS_CONID) {
      r = conid(b, 0);
    }
    else if (t == HS_CONOP) {
      r = conop(b, 0);
    }
    else if (t == HS_CONSTANT_FOLDED_PRAGMA) {
      r = constant_folded_pragma(b, 0);
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
    else if (t == HS_CONSYM) {
      r = consym(b, 0);
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
    else if (t == HS_DUMMY_PRAGMA) {
      r = dummy_pragma(b, 0);
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
    else if (t == HS_FIXITY_DECLARATION) {
      r = fixity_declaration(b, 0);
    }
    else if (t == HS_FOREIGN_DECLARATION) {
      r = foreign_declaration(b, 0);
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
    else if (t == HS_IMPORT_DECLARATION) {
      r = import_declaration(b, 0);
    }
    else if (t == HS_IMPORT_DECLARATIONS) {
      r = import_declarations(b, 0);
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
    else if (t == HS_IMPORT_PACKAGE_NAME) {
      r = import_package_name(b, 0);
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
    else if (t == HS_INCOHERENT_PRAGMA) {
      r = incoherent_pragma(b, 0);
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
    else if (t == HS_KIND_SIGNATURE) {
      r = kind_signature(b, 0);
    }
    else if (t == HS_LANGUAGE_PRAGMA) {
      r = language_pragma(b, 0);
    }
    else if (t == HS_LINE_PRAGMA) {
      r = line_pragma(b, 0);
    }
    else if (t == HS_LIST_TYPE) {
      r = list_type(b, 0);
    }
    else if (t == HS_MINIMAL_PRAGMA) {
      r = minimal_pragma(b, 0);
    }
    else if (t == HS_MODID) {
      r = modid(b, 0);
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
    else if (t == HS_OPTIONS_GHC_OPTION) {
      r = options_ghc_option(b, 0);
    }
    else if (t == HS_OPTIONS_GHC_PRAGMA) {
      r = options_ghc_pragma(b, 0);
    }
    else if (t == HS_OTHER_PRAGMA) {
      r = other_pragma(b, 0);
    }
    else if (t == HS_OVERLAP_PRAGMA) {
      r = overlap_pragma(b, 0);
    }
    else if (t == HS_Q_CON) {
      r = q_con(b, 0);
    }
    else if (t == HS_Q_CON_QUALIFIER) {
      r = q_con_qualifier(b, 0);
    }
    else if (t == HS_Q_CON_QUALIFIER_1) {
      r = q_con_qualifier1(b, 0);
    }
    else if (t == HS_Q_CON_QUALIFIER_2) {
      r = q_con_qualifier2(b, 0);
    }
    else if (t == HS_Q_CON_QUALIFIER_3) {
      r = q_con_qualifier3(b, 0);
    }
    else if (t == HS_Q_CON_QUALIFIER_4) {
      r = q_con_qualifier4(b, 0);
    }
    else if (t == HS_Q_NAME) {
      r = q_name(b, 0);
    }
    else if (t == HS_Q_NAMES) {
      r = q_names(b, 0);
    }
    else if (t == HS_Q_VAR_CON) {
      r = q_var_con(b, 0);
    }
    else if (t == HS_QUALIFIER) {
      r = qualifier(b, 0);
    }
    else if (t == HS_RESERVED_ID) {
      r = reserved_id(b, 0);
    }
    else if (t == HS_RULES_PRAGMA) {
      r = rules_pragma(b, 0);
    }
    else if (t == HS_SCC_PRAGMA) {
      r = scc_pragma(b, 0);
    }
    else if (t == HS_SCONTEXT) {
      r = scontext(b, 0);
    }
    else if (t == HS_SIMPLECLASS) {
      r = simpleclass(b, 0);
    }
    else if (t == HS_SIMPLETYPE) {
      r = simpletype(b, 0);
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
    else if (t == HS_TYPE_INSTANCE_DECLARATION) {
      r = type_instance_declaration(b, 0);
    }
    else if (t == HS_TYPE_SIGNATURE) {
      r = type_signature(b, 0);
    }
    else if (t == HS_UNPACK_NOUNPACK_PRAGMA) {
      r = unpack_nounpack_pragma(b, 0);
    }
    else if (t == HS_UNPACK_PRAGMA) {
      r = unpack_pragma(b, 0);
    }
    else if (t == HS_VAR) {
      r = var(b, 0);
    }
    else if (t == HS_VAR_CON) {
      r = var_con(b, 0);
    }
    else if (t == HS_VARID) {
      r = varid(b, 0);
    }
    else if (t == HS_VAROP) {
      r = varop(b, 0);
    }
    else if (t == HS_VARSYM) {
      r = varsym(b, 0);
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
  // "forall" (q_name | ttype | LEFT_PAREN type_signature RIGHT_PAREN)+ DOT |
  //                                   oonls LEFT_PAREN oonls ttype+ oonls RIGHT_PAREN |
  //                                   oonls LEFT_PAREN oonls ttype+ DOUBLE_RIGHT_ARROW oonls ttype+ onls RIGHT_PAREN |
  //                                   oonls LEFT_PAREN (oonls VARSYM_ID)? oonls ttype (oonls COMMA oonls ttype)* oonls (VARSYM_ID oonls)? RIGHT_PAREN |  // VARSYM_ID? is optional #
  //                                   QUOTE? LEFT_BRACKET oonls ttype oonls RIGHT_BRACKET |
  //                                   QUOTE? q_name+ | type_signature | QUOTE? LEFT_PAREN RIGHT_PAREN | QUOTE? LEFT_BRACKET RIGHT_BRACKET | LEFT_PAREN COMMA+ RIGHT_PAREN | DIRECTIVE
  static boolean atype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_0(b, l + 1);
    if (!r) r = atype_1(b, l + 1);
    if (!r) r = atype_2(b, l + 1);
    if (!r) r = atype_3(b, l + 1);
    if (!r) r = atype_4(b, l + 1);
    if (!r) r = atype_5(b, l + 1);
    if (!r) r = type_signature(b, l + 1);
    if (!r) r = atype_7(b, l + 1);
    if (!r) r = atype_8(b, l + 1);
    if (!r) r = atype_9(b, l + 1);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
    exit_section_(b, m, null, r);
    return r;
  }

  // "forall" (q_name | ttype | LEFT_PAREN type_signature RIGHT_PAREN)+ DOT
  private static boolean atype_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "forall");
    r = r && atype_0_1(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (q_name | ttype | LEFT_PAREN type_signature RIGHT_PAREN)+
  private static boolean atype_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_0_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!atype_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name | ttype | LEFT_PAREN type_signature RIGHT_PAREN
  private static boolean atype_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    if (!r) r = ttype(b, l + 1);
    if (!r) r = atype_0_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN type_signature RIGHT_PAREN
  private static boolean atype_0_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_0_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && type_signature(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // oonls LEFT_PAREN oonls ttype+ oonls RIGHT_PAREN
  private static boolean atype_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && oonls(b, l + 1);
    r = r && atype_1_3(b, l + 1);
    r = r && oonls(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype+
  private static boolean atype_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ttype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_1_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // oonls LEFT_PAREN oonls ttype+ DOUBLE_RIGHT_ARROW oonls ttype+ onls RIGHT_PAREN
  private static boolean atype_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && oonls(b, l + 1);
    r = r && atype_2_3(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    r = r && oonls(b, l + 1);
    r = r && atype_2_6(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype+
  private static boolean atype_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ttype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_2_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype+
  private static boolean atype_2_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_2_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ttype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_2_6", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // oonls LEFT_PAREN (oonls VARSYM_ID)? oonls ttype (oonls COMMA oonls ttype)* oonls (VARSYM_ID oonls)? RIGHT_PAREN
  private static boolean atype_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && atype_3_2(b, l + 1);
    r = r && oonls(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && atype_3_5(b, l + 1);
    r = r && oonls(b, l + 1);
    r = r && atype_3_7(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (oonls VARSYM_ID)?
  private static boolean atype_3_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_2")) return false;
    atype_3_2_0(b, l + 1);
    return true;
  }

  // oonls VARSYM_ID
  private static boolean atype_3_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls(b, l + 1);
    r = r && consumeToken(b, HS_VARSYM_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // (oonls COMMA oonls ttype)*
  private static boolean atype_3_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!atype_3_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_3_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // oonls COMMA oonls ttype
  private static boolean atype_3_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && oonls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VARSYM_ID oonls)?
  private static boolean atype_3_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_7")) return false;
    atype_3_7_0(b, l + 1);
    return true;
  }

  // VARSYM_ID oonls
  private static boolean atype_3_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_3_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VARSYM_ID);
    r = r && oonls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE? LEFT_BRACKET oonls ttype oonls RIGHT_BRACKET
  private static boolean atype_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_4_0(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_BRACKET);
    r = r && oonls(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && oonls(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE?
  private static boolean atype_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_4_0")) return false;
    consumeToken(b, HS_QUOTE);
    return true;
  }

  // QUOTE? q_name+
  private static boolean atype_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_5_0(b, l + 1);
    r = r && atype_5_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE?
  private static boolean atype_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_5_0")) return false;
    consumeToken(b, HS_QUOTE);
    return true;
  }

  // q_name+
  private static boolean atype_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_5_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "atype_5_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE? LEFT_PAREN RIGHT_PAREN
  private static boolean atype_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_7_0(b, l + 1);
    r = r && consumeTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE?
  private static boolean atype_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_7_0")) return false;
    consumeToken(b, HS_QUOTE);
    return true;
  }

  // QUOTE? LEFT_BRACKET RIGHT_BRACKET
  private static boolean atype_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = atype_8_0(b, l + 1);
    r = r && consumeTokens(b, 0, HS_LEFT_BRACKET, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // QUOTE?
  private static boolean atype_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_8_0")) return false;
    consumeToken(b, HS_QUOTE);
    return true;
  }

  // LEFT_PAREN COMMA+ RIGHT_PAREN
  private static boolean atype_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && atype_9_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA+
  private static boolean atype_9_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atype_9_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, HS_COMMA)) break;
      if (!empty_element_parsed_guard_(b, "atype_9_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // import_declarations top_declarations (NEWLINE | DIRECTIVE)*
  static boolean body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "body")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declarations(b, l + 1);
    r = r && top_declarations(b, l + 1);
    r = r && body_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (NEWLINE | DIRECTIVE)*
  private static boolean body_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "body_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!body_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "body_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // NEWLINE | DIRECTIVE
  private static boolean body_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "body_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_NEWLINE);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
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
    int c = current_position_(b);
    while (true) {
      if (!ccontext_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ccontext_0_3", c)) break;
      c = current_position_(b);
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
  // inline_pragma | noinline_pragma | specialize_pragma | instance_declaration | default_declaration |
  //                                   newtype_declaration | data_declaration | minimal_pragma | expression
  static boolean cidecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cidecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_pragma(b, l + 1);
    if (!r) r = noinline_pragma(b, l + 1);
    if (!r) r = specialize_pragma(b, l + 1);
    if (!r) r = instance_declaration(b, l + 1);
    if (!r) r = default_declaration(b, l + 1);
    if (!r) r = newtype_declaration(b, l + 1);
    if (!r) r = data_declaration(b, l + 1);
    if (!r) r = minimal_pragma(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // cidecl (onls cidecl)*
  public static boolean cidecls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cidecls")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CIDECLS, "<cidecls>");
    r = cidecl(b, l + 1);
    r = r && cidecls_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (onls cidecl)*
  private static boolean cidecls_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cidecls_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!cidecls_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "cidecls_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onls cidecl
  private static boolean cidecls_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cidecls_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && cidecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CLASS onls (scontext onls DOUBLE_RIGHT_ARROW)? onls (q_name+ | ttype) onls (q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
  //                                     (onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*)? onls WHERE? onls cidecls? |
  //                                   CLASS onls scontext onls DOUBLE_RIGHT_ARROW onls (q_name+ | ttype) onls WHERE? onls cidecls?
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

  // CLASS onls (scontext onls DOUBLE_RIGHT_ARROW)? onls (q_name+ | ttype) onls (q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
  //                                     (onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*)? onls WHERE? onls cidecls?
  private static boolean class_declaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CLASS);
    r = r && onls(b, l + 1);
    r = r && class_declaration_0_2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && class_declaration_0_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && class_declaration_0_6(b, l + 1);
    r = r && class_declaration_0_7(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && class_declaration_0_9(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && class_declaration_0_11(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext onls DOUBLE_RIGHT_ARROW)?
  private static boolean class_declaration_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_2")) return false;
    class_declaration_0_2_0(b, l + 1);
    return true;
  }

  // scontext onls DOUBLE_RIGHT_ARROW
  private static boolean class_declaration_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name+ | ttype
  private static boolean class_declaration_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = class_declaration_0_4_0(b, l + 1);
    if (!r) r = ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name+
  private static boolean class_declaration_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "class_declaration_0_4_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)*
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

  // q_name | LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
  private static boolean class_declaration_0_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    if (!r) r = class_declaration_0_6_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
  private static boolean class_declaration_0_6_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && ttype(b, l + 1);
    r = r && class_declaration_0_6_0_1_2(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls COMMA onls ttype)*
  private static boolean class_declaration_0_6_0_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0_1_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!class_declaration_0_6_0_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "class_declaration_0_6_0_1_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onls COMMA onls ttype
  private static boolean class_declaration_0_6_0_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_6_0_1_2_0")) return false;
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
  private static boolean class_declaration_0_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7")) return false;
    class_declaration_0_7_0(b, l + 1);
    return true;
  }

  // onls VERTICAL_BAR onls ttype (onls COMMA onls ttype)*
  private static boolean class_declaration_0_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && consumeToken(b, HS_VERTICAL_BAR);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && class_declaration_0_7_0_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls COMMA onls ttype)*
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

  // onls COMMA onls ttype
  private static boolean class_declaration_0_7_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_7_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && onls(b, l + 1);
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

  // cidecls?
  private static boolean class_declaration_0_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_0_11")) return false;
    cidecls(b, l + 1);
    return true;
  }

  // CLASS onls scontext onls DOUBLE_RIGHT_ARROW onls (q_name+ | ttype) onls WHERE? onls cidecls?
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
    r = r && onls(b, l + 1);
    r = r && class_declaration_1_8(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && class_declaration_1_10(b, l + 1);
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
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "class_declaration_1_6_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // WHERE?
  private static boolean class_declaration_1_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_1_8")) return false;
    consumeToken(b, HS_WHERE);
    return true;
  }

  // cidecls?
  private static boolean class_declaration_1_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_declaration_1_10")) return false;
    cidecls(b, l + 1);
    return true;
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
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_2", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!atype(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_3_3", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_4_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name*
  private static boolean clazz_4_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "clazz_4_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "clazz_4_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // var | con | conop | varop
  public static boolean cname(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cname")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CNAME, "<cname>");
    r = var(b, l + 1);
    if (!r) r = con(b, l + 1);
    if (!r) r = conop(b, l + 1);
    if (!r) r = varop(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMENT | NCOMMENT | NCOMMENT_START | NCOMMENT_END | HADDOCK | NHADDOCK
  public static boolean comments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_COMMENTS, "<comments>");
    r = consumeToken(b, HS_COMMENT);
    if (!r) r = consumeToken(b, HS_NCOMMENT);
    if (!r) r = consumeToken(b, HS_NCOMMENT_START);
    if (!r) r = consumeToken(b, HS_NCOMMENT_END);
    if (!r) r = consumeToken(b, HS_HADDOCK);
    if (!r) r = consumeToken(b, HS_NHADDOCK);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // conid | LEFT_PAREN consym RIGHT_PAREN
  public static boolean con(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con")) return false;
    if (!nextTokenIs(b, "<con>", HS_CON_ID, HS_LEFT_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CON, "<con>");
    r = conid(b, l + 1);
    if (!r) r = con_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_PAREN consym RIGHT_PAREN
  private static boolean con_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "con_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && consym(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
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
  // consym | BACKQUOTE conid BACKQUOTE
  public static boolean conop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conop")) return false;
    if (!nextTokenIs(b, "<conop>", HS_BACKQUOTE, HS_CONSYM_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CONOP, "<conop>");
    r = consym(b, l + 1);
    if (!r) r = conop_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BACKQUOTE conid BACKQUOTE
  private static boolean conop_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conop_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && conid(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "CONSTANT_FOLDED" onl general_pragma_content onl PRAGMA_END
  public static boolean constant_folded_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constant_folded_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "CONSTANT_FOLDED");
    r = r && onl(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_CONSTANT_FOLDED_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // type_signature | constr1 | constr2 | constr3 | constr4
  static boolean constr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_signature(b, l + 1);
    if (!r) r = constr1(b, l + 1);
    if (!r) r = constr2(b, l + 1);
    if (!r) r = constr3(b, l + 1);
    if (!r) r = constr4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? q_name onls unpack_nounpack_pragma? onls LEFT_BRACE onl (fielddecl ((onl COMMA)? onl fielddecl)*)? onl RIGHT_BRACE
  public static boolean constr1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_1, "<constr 1>");
    r = constr1_0(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && constr1_3(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_BRACE);
    r = r && onl(b, l + 1);
    r = r && constr1_7(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
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

  // (fielddecl ((onl COMMA)? onl fielddecl)*)?
  private static boolean constr1_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7")) return false;
    constr1_7_0(b, l + 1);
    return true;
  }

  // fielddecl ((onl COMMA)? onl fielddecl)*
  private static boolean constr1_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fielddecl(b, l + 1);
    r = r && constr1_7_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ((onl COMMA)? onl fielddecl)*
  private static boolean constr1_7_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constr1_7_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr1_7_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (onl COMMA)? onl fielddecl
  private static boolean constr1_7_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constr1_7_0_1_0_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && fielddecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onl COMMA)?
  private static boolean constr1_7_0_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_1_0_0")) return false;
    constr1_7_0_1_0_0_0(b, l + 1);
    return true;
  }

  // onl COMMA
  private static boolean constr1_7_0_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr1_7_0_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? (q_name | LEFT_PAREN q_name* RIGHT_PAREN | LEFT_BRACKET q_name* RIGHT_BRACKET) onls (onls unpack_nounpack_pragma? onls ttype)*
  public static boolean constr2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_2, "<constr 2>");
    r = constr2_0(b, l + 1);
    r = r && constr2_1(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && constr2_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // q_name | LEFT_PAREN q_name* RIGHT_PAREN | LEFT_BRACKET q_name* RIGHT_BRACKET
  private static boolean constr2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    if (!r) r = constr2_1_1(b, l + 1);
    if (!r) r = constr2_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN q_name* RIGHT_PAREN
  private static boolean constr2_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && constr2_1_1_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name*
  private static boolean constr2_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_1_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr2_1_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_BRACKET q_name* RIGHT_BRACKET
  private static boolean constr2_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && constr2_1_2_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name*
  private static boolean constr2_1_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_1_2_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr2_1_2_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (onls unpack_nounpack_pragma? onls ttype)*
  private static boolean constr2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constr2_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr2_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onls unpack_nounpack_pragma? onls ttype
  private static boolean constr2_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && constr2_3_0_1(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr2_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr2_3_0_1")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? sub_constr2 onls unpack_nounpack_pragma? q_name onls unpack_nounpack_pragma? onls sub_constr2
  public static boolean constr3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_3, "<constr 3>");
    r = constr3_0(b, l + 1);
    r = r && sub_constr2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && constr3_3(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && constr3_6(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && sub_constr2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // unpack_nounpack_pragma?
  private static boolean constr3_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_3")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // unpack_nounpack_pragma?
  private static boolean constr3_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr3_6")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // unpack_nounpack_pragma? q_name+ unpack_nounpack_pragma? q_name
  public static boolean constr4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_CONSTR_4, "<constr 4>");
    r = constr4_0(b, l + 1);
    r = r && constr4_1(b, l + 1);
    r = r && constr4_2(b, l + 1);
    r = r && q_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4_0")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  // q_name+
  private static boolean constr4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constr4_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // unpack_nounpack_pragma?
  private static boolean constr4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constr4_2")) return false;
    unpack_nounpack_pragma(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (("forall" | "∀") q_name DOT)? constr (onls VERTICAL_BAR onls constr)*
  static boolean constrs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constrs_0(b, l + 1);
    r = r && constr(b, l + 1);
    r = r && constrs_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (("forall" | "∀") q_name DOT)?
  private static boolean constrs_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_0")) return false;
    constrs_0_0(b, l + 1);
    return true;
  }

  // ("forall" | "∀") q_name DOT
  private static boolean constrs_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constrs_0_0_0(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // "forall" | "∀"
  private static boolean constrs_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "forall");
    if (!r) r = consumeToken(b, "∀");
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls VERTICAL_BAR onls constr)*
  private static boolean constrs_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!constrs_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constrs_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onls VERTICAL_BAR onls constr
  private static boolean constrs_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constrs_2_0")) return false;
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
  // PRAGMA_START onl "CTYPE" onl general_pragma_content onl PRAGMA_END
  public static boolean ctype_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ctype_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "CTYPE");
    r = r && onl(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_CTYPE_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // DATA (onls ctype_pragma)? onls INSTANCE? onls (ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls q_name* onls (EQUAL | WHERE)?
  //                                     onls (scontext onls DOUBLE_RIGHT_ARROW)? onls (constrs | simpletype | cidecls)? (onls data_declaration_deriving)? |
  //                                   DATA (onls ctype_pragma)? onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls q_name* (onls data_declaration_deriving)?
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

  // DATA (onls ctype_pragma)? onls INSTANCE? onls (ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls q_name* onls (EQUAL | WHERE)?
  //                                     onls (scontext onls DOUBLE_RIGHT_ARROW)? onls (constrs | simpletype | cidecls)? (onls data_declaration_deriving)?
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
    r = r && onls(b, l + 1);
    r = r && data_declaration_0_9(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_0_11(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_0_13(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_0_15(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_0_17(b, l + 1);
    r = r && data_declaration_0_18(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls ctype_pragma)?
  private static boolean data_declaration_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_1")) return false;
    data_declaration_0_1_0(b, l + 1);
    return true;
  }

  // onls ctype_pragma
  private static boolean data_declaration_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && ctype_pragma(b, l + 1);
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

  // (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
  private static boolean data_declaration_0_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_9")) return false;
    int c = current_position_(b);
    while (true) {
      if (!data_declaration_0_9_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_0_9", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_PAREN onls kind_signature onls RIGHT_PAREN
  private static boolean data_declaration_0_9_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_9_0")) return false;
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

  // q_name*
  private static boolean data_declaration_0_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_11")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_0_11", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (EQUAL | WHERE)?
  private static boolean data_declaration_0_13(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_13")) return false;
    data_declaration_0_13_0(b, l + 1);
    return true;
  }

  // EQUAL | WHERE
  private static boolean data_declaration_0_13_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_13_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_EQUAL);
    if (!r) r = consumeToken(b, HS_WHERE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext onls DOUBLE_RIGHT_ARROW)?
  private static boolean data_declaration_0_15(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_15")) return false;
    data_declaration_0_15_0(b, l + 1);
    return true;
  }

  // scontext onls DOUBLE_RIGHT_ARROW
  private static boolean data_declaration_0_15_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_15_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (constrs | simpletype | cidecls)?
  private static boolean data_declaration_0_17(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_17")) return false;
    data_declaration_0_17_0(b, l + 1);
    return true;
  }

  // constrs | simpletype | cidecls
  private static boolean data_declaration_0_17_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_17_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constrs(b, l + 1);
    if (!r) r = simpletype(b, l + 1);
    if (!r) r = cidecls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls data_declaration_deriving)?
  private static boolean data_declaration_0_18(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_18")) return false;
    data_declaration_0_18_0(b, l + 1);
    return true;
  }

  // onls data_declaration_deriving
  private static boolean data_declaration_0_18_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_0_18_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && data_declaration_deriving(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DATA (onls ctype_pragma)? onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls q_name* (onls data_declaration_deriving)?
  private static boolean data_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DATA);
    r = r && data_declaration_1_1(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_1_3(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_1_7(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && data_declaration_1_9(b, l + 1);
    r = r && data_declaration_1_10(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls ctype_pragma)?
  private static boolean data_declaration_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_1")) return false;
    data_declaration_1_1_0(b, l + 1);
    return true;
  }

  // onls ctype_pragma
  private static boolean data_declaration_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && ctype_pragma(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
  private static boolean data_declaration_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!data_declaration_1_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_1_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_PAREN onls kind_signature onls RIGHT_PAREN
  private static boolean data_declaration_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_3_0")) return false;
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

  // (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
  private static boolean data_declaration_1_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_7")) return false;
    int c = current_position_(b);
    while (true) {
      if (!data_declaration_1_7_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_1_7", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_PAREN onls kind_signature onls RIGHT_PAREN
  private static boolean data_declaration_1_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_7_0")) return false;
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

  // q_name*
  private static boolean data_declaration_1_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_9")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_1_9", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (onls data_declaration_deriving)?
  private static boolean data_declaration_1_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_10")) return false;
    data_declaration_1_10_0(b, l + 1);
    return true;
  }

  // onls data_declaration_deriving
  private static boolean data_declaration_1_10_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_1_10_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && data_declaration_deriving(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (DERIVING onls ttype | DERIVING onls LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN)+
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

  // DERIVING onls ttype | DERIVING onls LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
  private static boolean data_declaration_deriving_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = data_declaration_deriving_0_0(b, l + 1);
    if (!r) r = data_declaration_deriving_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DERIVING onls ttype
  private static boolean data_declaration_deriving_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DERIVING);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DERIVING onls LEFT_PAREN ttype (onls COMMA onls ttype)* RIGHT_PAREN
  private static boolean data_declaration_deriving_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DERIVING);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && ttype(b, l + 1);
    r = r && data_declaration_deriving_0_1_4(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onls COMMA onls ttype)*
  private static boolean data_declaration_deriving_0_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!data_declaration_deriving_0_1_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "data_declaration_deriving_0_1_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onls COMMA onls ttype
  private static boolean data_declaration_deriving_0_1_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "data_declaration_deriving_0_1_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DEFAULT onls (LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN | type_signature)
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

  // LEFT_PAREN (ttype (COMMA ttype)*)? RIGHT_PAREN | type_signature
  private static boolean default_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_declaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = default_declaration_2_0(b, l + 1);
    if (!r) r = type_signature(b, l + 1);
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
  // LEFT_PAREN DOT_DOT RIGHT_PAREN
  public static boolean dot_dot_parens(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dot_dot_parens")) return false;
    if (!nextTokenIs(b, HS_LEFT_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_LEFT_PAREN, HS_DOT_DOT, HS_RIGHT_PAREN);
    exit_section_(b, m, HS_DOT_DOT_PARENS, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START q_name? PRAGMA_END? NEWLINE?
  public static boolean dummy_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && dummy_pragma_1(b, l + 1);
    r = r && dummy_pragma_2(b, l + 1);
    r = r && dummy_pragma_3(b, l + 1);
    exit_section_(b, m, HS_DUMMY_PRAGMA, r);
    return r;
  }

  // q_name?
  private static boolean dummy_pragma_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_pragma_1")) return false;
    q_name(b, l + 1);
    return true;
  }

  // PRAGMA_END?
  private static boolean dummy_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_pragma_2")) return false;
    consumeToken(b, HS_PRAGMA_END);
    return true;
  }

  // NEWLINE?
  private static boolean dummy_pragma_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_pragma_3")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // export3 | export1 | export2 | export4
  public static boolean export(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_EXPORT, "<export>");
    r = export3(b, l + 1);
    if (!r) r = export1(b, l + 1);
    if (!r) r = export2(b, l + 1);
    if (!r) r = export4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (q_con | cname) dot_dot_parens
  static boolean export1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = export1_0(b, l + 1);
    r = r && dot_dot_parens(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_con | cname
  private static boolean export1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con(b, l + 1);
    if (!r) r = cname(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TYPE? onl cname+
  static boolean export2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = export2_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TYPE?
  private static boolean export2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_0")) return false;
    consumeToken(b, HS_TYPE);
    return true;
  }

  // cname+
  private static boolean export2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export2_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!cname(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "export2_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (q_con | cname) onl LEFT_PAREN onl (cname onl (onl COMMA onl cname)*)? onl RIGHT_PAREN
  static boolean export3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = export3_0(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && onl(b, l + 1);
    r = r && export3_4(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_con | cname
  private static boolean export3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con(b, l + 1);
    if (!r) r = cname(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (cname onl (onl COMMA onl cname)*)?
  private static boolean export3_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3_4")) return false;
    export3_4_0(b, l + 1);
    return true;
  }

  // cname onl (onl COMMA onl cname)*
  private static boolean export3_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export3_4_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onl COMMA onl cname)*
  private static boolean export3_4_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3_4_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!export3_4_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "export3_4_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA onl cname
  private static boolean export3_4_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export3_4_0_2_0")) return false;
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
  // MODULE modid
  static boolean export4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export4")) return false;
    if (!nextTokenIs(b, HS_MODULE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_MODULE);
    r = r && modid(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PAREN onl export (onl COMMA? onl export onl)* onl COMMA? onl RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN
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

  // LEFT_PAREN onl export (onl COMMA? onl export onl)* onl COMMA? onl RIGHT_PAREN
  private static boolean exports_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && onl(b, l + 1);
    r = r && export(b, l + 1);
    r = r && exports_0_3(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && exports_0_5(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (onl COMMA? onl export onl)*
  private static boolean exports_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_0_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!exports_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exports_0_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // onl COMMA? onl export onl
  private static boolean exports_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && exports_0_3_0_1(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && export(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean exports_0_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_0_3_0_1")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  // COMMA?
  private static boolean exports_0_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exports_0_5")) return false;
    consumeToken(b, HS_COMMA);
    return true;
  }

  /* ********************************************************** */
  // line_expression+ last_line_expression | last_line_expression
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_EXPRESSION, "<expression>");
    r = expression_0(b, l + 1);
    if (!r) r = last_line_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // line_expression+ last_line_expression
  private static boolean expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression_0_0(b, l + 1);
    r = r && last_line_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // line_expression+
  private static boolean expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = line_expression(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!line_expression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // q_names (COLON_COLON unpack_nounpack_pragma? (ttype | q_name atype))?
  public static boolean fielddecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_FIELDDECL, "<fielddecl>");
    r = q_names(b, l + 1);
    r = r && fielddecl_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COLON_COLON unpack_nounpack_pragma? (ttype | q_name atype))?
  private static boolean fielddecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1")) return false;
    fielddecl_1_0(b, l + 1);
    return true;
  }

  // COLON_COLON unpack_nounpack_pragma? (ttype | q_name atype)
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

  // ttype | q_name atype
  private static boolean fielddecl_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    if (!r) r = fielddecl_1_0_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name atype
  private static boolean fielddecl_1_0_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fielddecl_1_0_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    r = r && atype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (file_header_pragma onl)+
  public static boolean file_header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header")) return false;
    if (!nextTokenIs(b, "<file header>", HS_DIRECTIVE, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_FILE_HEADER, "<file header>");
    r = file_header_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!file_header_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file_header", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // file_header_pragma onl
  private static boolean file_header_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = file_header_pragma(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // language_pragma | options_ghc_pragma | include_pragma | haddock_pragma | ann_pragma | DIRECTIVE | dummy_pragma
  public static boolean file_header_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_header_pragma")) return false;
    if (!nextTokenIs(b, "<file header pragma>", HS_DIRECTIVE, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_FILE_HEADER_PRAGMA, "<file header pragma>");
    r = language_pragma(b, l + 1);
    if (!r) r = options_ghc_pragma(b, l + 1);
    if (!r) r = include_pragma(b, l + 1);
    if (!r) r = haddock_pragma(b, l + 1);
    if (!r) r = ann_pragma(b, l + 1);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
    if (!r) r = dummy_pragma(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // INFIXL | INFIXR | INFIX
  static boolean fixity(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fixity")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_INFIXL);
    if (!r) r = consumeToken(b, HS_INFIXR);
    if (!r) r = consumeToken(b, HS_INFIX);
    exit_section_(b, m, null, r);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_FOREIGN_IMPORT);
    if (!r) r = consumeToken(b, HS_FOREIGN_EXPORT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // q_name | LEFT_PAREN | RIGHT_PAREN | FLOAT |
  //                                   DOUBLE_RIGHT_ARROW | RIGHT_ARROW |
  //                                   SEMICOLON | LEFT_ARROW | LEFT_BRACKET | RIGHT_BRACKET | literal | LEFT_BRACE | RIGHT_BRACE |
  //                                   COMMA | symbol_reserved_op | QUOTE | BACKQUOTE | fixity | DOT_DOT | scc_pragma | reserved_id | DIRECTIVE | COLON_COLON
  static boolean general_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_id")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    if (!r) r = consumeToken(b, HS_LEFT_PAREN);
    if (!r) r = consumeToken(b, HS_RIGHT_PAREN);
    if (!r) r = consumeToken(b, HS_FLOAT);
    if (!r) r = consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    if (!r) r = consumeToken(b, HS_RIGHT_ARROW);
    if (!r) r = consumeToken(b, HS_SEMICOLON);
    if (!r) r = consumeToken(b, HS_LEFT_ARROW);
    if (!r) r = consumeToken(b, HS_LEFT_BRACKET);
    if (!r) r = consumeToken(b, HS_RIGHT_BRACKET);
    if (!r) r = literal(b, l + 1);
    if (!r) r = consumeToken(b, HS_LEFT_BRACE);
    if (!r) r = consumeToken(b, HS_RIGHT_BRACE);
    if (!r) r = consumeToken(b, HS_COMMA);
    if (!r) r = symbol_reserved_op(b, l + 1);
    if (!r) r = consumeToken(b, HS_QUOTE);
    if (!r) r = consumeToken(b, HS_BACKQUOTE);
    if (!r) r = fixity(b, l + 1);
    if (!r) r = consumeToken(b, HS_DOT_DOT);
    if (!r) r = scc_pragma(b, l + 1);
    if (!r) r = reserved_id(b, l + 1);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
    if (!r) r = consumeToken(b, HS_COLON_COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (CON_ID | VAR_ID | CONSYM_ID | VARSYM_ID | DOT |
  //                                   LEFT_PAREN | RIGHT_PAREN | FLOAT | DO | WHERE | IF | THEN | ELSE |
  //                                   COLON_COLON | DOUBLE_RIGHT_ARROW | RIGHT_ARROW | IN | CASE | OF | LET |
  //                                   SEMICOLON | LEFT_ARROW | LEFT_BRACKET | RIGHT_BRACKET | literal | LEFT_BRACE | RIGHT_BRACE |
  //                                   COMMA | UNDERSCORE | symbol_reserved_op | QUOTE | BACKQUOTE | DOT_DOT | MODULE | INSTANCE | NEWLINE)+
  public static boolean general_pragma_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_pragma_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_GENERAL_PRAGMA_CONTENT, "<general pragma content>");
    r = general_pragma_content_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_pragma_content_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "general_pragma_content", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CON_ID | VAR_ID | CONSYM_ID | VARSYM_ID | DOT |
  //                                   LEFT_PAREN | RIGHT_PAREN | FLOAT | DO | WHERE | IF | THEN | ELSE |
  //                                   COLON_COLON | DOUBLE_RIGHT_ARROW | RIGHT_ARROW | IN | CASE | OF | LET |
  //                                   SEMICOLON | LEFT_ARROW | LEFT_BRACKET | RIGHT_BRACKET | literal | LEFT_BRACE | RIGHT_BRACE |
  //                                   COMMA | UNDERSCORE | symbol_reserved_op | QUOTE | BACKQUOTE | DOT_DOT | MODULE | INSTANCE | NEWLINE
  private static boolean general_pragma_content_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_pragma_content_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CON_ID);
    if (!r) r = consumeToken(b, HS_VAR_ID);
    if (!r) r = consumeToken(b, HS_CONSYM_ID);
    if (!r) r = consumeToken(b, HS_VARSYM_ID);
    if (!r) r = consumeToken(b, HS_DOT);
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
    if (!r) r = consumeToken(b, HS_DOT_DOT);
    if (!r) r = consumeToken(b, HS_MODULE);
    if (!r) r = consumeToken(b, HS_INSTANCE);
    if (!r) r = consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, HS_COMMA)) break;
      if (!empty_element_parsed_guard_(b, "gtycon_4_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "OPTIONS_HADDOCK" onl (VARSYM_ID | VAR_ID)+ onl (COMMA onl (VARSYM_ID | VAR_ID)+)* onl PRAGMA_END NEWLINE?
  public static boolean haddock_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "OPTIONS_HADDOCK");
    r = r && onl(b, l + 1);
    r = r && haddock_pragma_4(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && haddock_pragma_6(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    r = r && haddock_pragma_9(b, l + 1);
    exit_section_(b, m, HS_HADDOCK_PRAGMA, r);
    return r;
  }

  // (VARSYM_ID | VAR_ID)+
  private static boolean haddock_pragma_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = haddock_pragma_4_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!haddock_pragma_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "haddock_pragma_4", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // VARSYM_ID | VAR_ID
  private static boolean haddock_pragma_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VARSYM_ID);
    if (!r) r = consumeToken(b, HS_VAR_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA onl (VARSYM_ID | VAR_ID)+)*
  private static boolean haddock_pragma_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!haddock_pragma_6_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "haddock_pragma_6", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA onl (VARSYM_ID | VAR_ID)+
  private static boolean haddock_pragma_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && onl(b, l + 1);
    r = r && haddock_pragma_6_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VARSYM_ID | VAR_ID)+
  private static boolean haddock_pragma_6_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_6_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = haddock_pragma_6_0_2_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!haddock_pragma_6_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "haddock_pragma_6_0_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // VARSYM_ID | VAR_ID
  private static boolean haddock_pragma_6_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_6_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VARSYM_ID);
    if (!r) r = consumeToken(b, HS_VAR_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean haddock_pragma_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "haddock_pragma_9")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // IMPORT onls source_pragma? import_package_name? onls import_qualified? onls modid onls import_qualified_as? onls import_spec? NEWLINE?
  public static boolean import_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration")) return false;
    if (!nextTokenIs(b, HS_IMPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_DECLARATION, null);
    r = consumeToken(b, HS_IMPORT);
    p = r; // pin = 1
    r = r && report_error_(b, onls(b, l + 1));
    r = p && report_error_(b, import_declaration_2(b, l + 1)) && r;
    r = p && report_error_(b, import_declaration_3(b, l + 1)) && r;
    r = p && report_error_(b, onls(b, l + 1)) && r;
    r = p && report_error_(b, import_declaration_5(b, l + 1)) && r;
    r = p && report_error_(b, onls(b, l + 1)) && r;
    r = p && report_error_(b, modid(b, l + 1)) && r;
    r = p && report_error_(b, onls(b, l + 1)) && r;
    r = p && report_error_(b, import_declaration_9(b, l + 1)) && r;
    r = p && report_error_(b, onls(b, l + 1)) && r;
    r = p && report_error_(b, import_declaration_11(b, l + 1)) && r;
    r = p && import_declaration_12(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // source_pragma?
  private static boolean import_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_2")) return false;
    source_pragma(b, l + 1);
    return true;
  }

  // import_package_name?
  private static boolean import_declaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_3")) return false;
    import_package_name(b, l + 1);
    return true;
  }

  // import_qualified?
  private static boolean import_declaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_5")) return false;
    import_qualified(b, l + 1);
    return true;
  }

  // import_qualified_as?
  private static boolean import_declaration_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_9")) return false;
    import_qualified_as(b, l + 1);
    return true;
  }

  // import_spec?
  private static boolean import_declaration_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_11")) return false;
    import_spec(b, l + 1);
    return true;
  }

  // NEWLINE?
  private static boolean import_declaration_12(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declaration_12")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // ((import_declaration | cfiles_pragma | DIRECTIVE) onl)*
  public static boolean import_declarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations")) return false;
    Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_DECLARATIONS, "<import declarations>");
    int c = current_position_(b);
    while (true) {
      if (!import_declarations_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_declarations", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // (import_declaration | cfiles_pragma | DIRECTIVE) onl
  private static boolean import_declarations_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declarations_0_0(b, l + 1);
    r = r && onl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // import_declaration | cfiles_pragma | DIRECTIVE
  private static boolean import_declarations_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_declarations_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_declaration(b, l + 1);
    if (!r) r = cfiles_pragma(b, l + 1);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
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
  // VAR_ID
  public static boolean import_hiding(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding")) return false;
    if (!nextTokenIs(b, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    exit_section_(b, m, HS_IMPORT_HIDING, r);
    return r;
  }

  /* ********************************************************** */
  // import_hiding onls LEFT_PAREN onls (import_id onls (onls COMMA onls import_id)* onls (COMMA)?)? onls RIGHT_PAREN
  public static boolean import_hiding_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_hiding_spec")) return false;
    if (!nextTokenIs(b, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_hiding(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_LEFT_PAREN);
    r = r && onls(b, l + 1);
    r = r && import_hiding_spec_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, HS_IMPORT_HIDING_SPEC, r);
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
    int c = current_position_(b);
    while (true) {
      if (!import_hiding_spec_4_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_hiding_spec_4_0_2", c)) break;
      c = current_position_(b);
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
  // TYPE? (cname onls dot_dot_parens | cname (LEFT_PAREN onls (cname onls (COMMA onls cname onls)* onls)? RIGHT_PAREN) | cname)
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

  // cname onls dot_dot_parens | cname (LEFT_PAREN onls (cname onls (COMMA onls cname onls)* onls)? RIGHT_PAREN) | cname
  private static boolean import_id_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = import_id_1_0(b, l + 1);
    if (!r) r = import_id_1_1(b, l + 1);
    if (!r) r = cname(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // cname onls dot_dot_parens
  private static boolean import_id_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && dot_dot_parens(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // cname (LEFT_PAREN onls (cname onls (COMMA onls cname onls)* onls)? RIGHT_PAREN)
  private static boolean import_id_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && import_id_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN onls (cname onls (COMMA onls cname onls)* onls)? RIGHT_PAREN
  private static boolean import_id_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && onls(b, l + 1);
    r = r && import_id_1_1_1_2(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (cname onls (COMMA onls cname onls)* onls)?
  private static boolean import_id_1_1_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1_1_2")) return false;
    import_id_1_1_1_2_0(b, l + 1);
    return true;
  }

  // cname onls (COMMA onls cname onls)* onls
  private static boolean import_id_1_1_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cname(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && import_id_1_1_1_2_0_2(b, l + 1);
    r = r && onls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA onls cname onls)*
  private static boolean import_id_1_1_1_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1_1_2_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!import_id_1_1_1_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_id_1_1_1_2_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA onls cname onls
  private static boolean import_id_1_1_1_2_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_id_1_1_1_2_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COMMA);
    r = r && onls(b, l + 1);
    r = r && cname(b, l + 1);
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
    int c = current_position_(b);
    while (true) {
      if (!import_ids_spec_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "import_ids_spec_3", c)) break;
      c = current_position_(b);
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
  // STRING_LITERAL
  public static boolean import_package_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_package_name")) return false;
    if (!nextTokenIs(b, HS_STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_STRING_LITERAL);
    exit_section_(b, m, HS_IMPORT_PACKAGE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // VAR_ID
  public static boolean import_qualified(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_qualified")) return false;
    if (!nextTokenIs(b, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    exit_section_(b, m, HS_IMPORT_QUALIFIED, r);
    return r;
  }

  /* ********************************************************** */
  // VAR_ID qualifier
  public static boolean import_qualified_as(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_qualified_as")) return false;
    if (!nextTokenIs(b, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    r = r && qualifier(b, l + 1);
    exit_section_(b, m, HS_IMPORT_QUALIFIED_AS, r);
    return r;
  }

  /* ********************************************************** */
  // import_ids_spec |
  //                                   import_hiding_spec |
  //                                   import_empty_spec
  public static boolean import_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_spec")) return false;
    if (!nextTokenIs(b, "<import spec>", HS_LEFT_PAREN, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_IMPORT_SPEC, "<import spec>");
    r = import_ids_spec(b, l + 1);
    if (!r) r = import_hiding_spec(b, l + 1);
    if (!r) r = import_empty_spec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "INCLUDE" general_pragma_content PRAGMA_END NEWLINE?
  public static boolean include_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "INCLUDE");
    r = r && general_pragma_content(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    r = r && include_pragma_5(b, l + 1);
    exit_section_(b, m, HS_INCLUDE_PRAGMA, r);
    return r;
  }

  // NEWLINE?
  private static boolean include_pragma_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_pragma_5")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "INCOHERENT" onl PRAGMA_END
  public static boolean incoherent_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incoherent_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "INCOHERENT");
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_INCOHERENT_PRAGMA, r);
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
  // gtycon+ instvar* (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)+ instvar* |
  //                                   (LEFT_PAREN onls instvar (onls COMMA onls instvar)+ onls RIGHT_PAREN)+ instvar* |
  //                                   QUOTE? (LEFT_BRACKET onls instvar onls RIGHT_BRACKET)+ instvar* |
  //                                   (LEFT_PAREN onls instvar+ onls RIGHT_PAREN)+ instvar* |
  //                                   (LEFT_PAREN onls instvar+ (RIGHT_ARROW onls instvar* onls)* onls RIGHT_PAREN)+ instvar* |
  //                                   ttype
  public static boolean inst(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_INST, "<inst>");
    r = inst_0(b, l + 1);
    if (!r) r = inst_1(b, l + 1);
    if (!r) r = inst_2(b, l + 1);
    if (!r) r = inst_3(b, l + 1);
    if (!r) r = inst_4(b, l + 1);
    if (!r) r = ttype(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // gtycon+ instvar* (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)+ instvar*
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
    int c = current_position_(b);
    while (r) {
      if (!gtycon(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN onls gtycon (instvar)* onls RIGHT_PAREN)+
  private static boolean inst_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_0_2_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_0_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!inst_0_2_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_0_2_0_3", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_0_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN onls instvar (onls COMMA onls instvar)+ onls RIGHT_PAREN)+ instvar*
  private static boolean inst_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_1_0(b, l + 1);
    r = r && inst_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN onls instvar (onls COMMA onls instvar)+ onls RIGHT_PAREN)+
  private static boolean inst_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_1_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_1_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN onls instvar (onls COMMA onls instvar)+ onls RIGHT_PAREN
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

  // (onls COMMA onls instvar)+
  private static boolean inst_1_0_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_1_0_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_1_0_0_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!inst_1_0_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_0_0_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_1_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!inst_2_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_2_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_2_2", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!inst_3_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_3_0", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_3_0_0_2", c)) break;
      c = current_position_(b);
    }
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

  // (LEFT_PAREN onls instvar+ (RIGHT_ARROW onls instvar* onls)* onls RIGHT_PAREN)+ instvar*
  private static boolean inst_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inst_4_0(b, l + 1);
    r = r && inst_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN onls instvar+ (RIGHT_ARROW onls instvar* onls)* onls RIGHT_PAREN)+
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

  // LEFT_PAREN onls instvar+ (RIGHT_ARROW onls instvar* onls)* onls RIGHT_PAREN
  private static boolean inst_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && onls(b, l + 1);
    r = r && inst_4_0_0_2(b, l + 1);
    r = r && inst_4_0_0_3(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar+
  private static boolean inst_4_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instvar(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_0_0_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (RIGHT_ARROW onls instvar* onls)*
  private static boolean inst_4_0_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!inst_4_0_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_0_0_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // RIGHT_ARROW onls instvar* onls
  private static boolean inst_4_0_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_RIGHT_ARROW);
    r = r && onls(b, l + 1);
    r = r && inst_4_0_0_3_0_2(b, l + 1);
    r = r && onls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instvar*
  private static boolean inst_4_0_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inst_4_0_0_3_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instvar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inst_4_0_0_3_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
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

  /* ********************************************************** */
  // INSTANCE onls (overlap_pragma | "OVERLAPPABLE_" | "OVERLAPPING_" | incoherent_pragma)? onls (var_con+ DOT)? onls (scontext onls DOUBLE_RIGHT_ARROW)? onls q_name onls inst onls (WHERE onls cidecls)?
  public static boolean instance_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration")) return false;
    if (!nextTokenIs(b, HS_INSTANCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_INSTANCE);
    r = r && onls(b, l + 1);
    r = r && instance_declaration_2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && instance_declaration_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && instance_declaration_6(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && inst(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && instance_declaration_12(b, l + 1);
    exit_section_(b, m, HS_INSTANCE_DECLARATION, r);
    return r;
  }

  // (overlap_pragma | "OVERLAPPABLE_" | "OVERLAPPING_" | incoherent_pragma)?
  private static boolean instance_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_2")) return false;
    instance_declaration_2_0(b, l + 1);
    return true;
  }

  // overlap_pragma | "OVERLAPPABLE_" | "OVERLAPPING_" | incoherent_pragma
  private static boolean instance_declaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = overlap_pragma(b, l + 1);
    if (!r) r = consumeToken(b, "OVERLAPPABLE_");
    if (!r) r = consumeToken(b, "OVERLAPPING_");
    if (!r) r = incoherent_pragma(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (var_con+ DOT)?
  private static boolean instance_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_4")) return false;
    instance_declaration_4_0(b, l + 1);
    return true;
  }

  // var_con+ DOT
  private static boolean instance_declaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instance_declaration_4_0_0(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // var_con+
  private static boolean instance_declaration_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_4_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = var_con(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!var_con(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "instance_declaration_4_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (scontext onls DOUBLE_RIGHT_ARROW)?
  private static boolean instance_declaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_6")) return false;
    instance_declaration_6_0(b, l + 1);
    return true;
  }

  // scontext onls DOUBLE_RIGHT_ARROW
  private static boolean instance_declaration_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scontext(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (WHERE onls cidecls)?
  private static boolean instance_declaration_12(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_12")) return false;
    instance_declaration_12_0(b, l + 1);
    return true;
  }

  // WHERE onls cidecls
  private static boolean instance_declaration_12_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instance_declaration_12_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_WHERE);
    r = r && onls(b, l + 1);
    r = r && cidecls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // q_name | LEFT_BRACKET q_name+ RIGHT_BRACKET | LEFT_PAREN q_name+ RIGHT_PAREN | LEFT_PAREN RIGHT_PAREN
  public static boolean instvar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_INSTVAR, "<instvar>");
    r = q_name(b, l + 1);
    if (!r) r = instvar_1(b, l + 1);
    if (!r) r = instvar_2(b, l + 1);
    if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_PAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_BRACKET q_name+ RIGHT_BRACKET
  private static boolean instvar_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_BRACKET);
    r = r && instvar_1_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name+
  private static boolean instvar_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "instvar_1_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN q_name+ RIGHT_PAREN
  private static boolean instvar_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && instvar_2_1(b, l + 1);
    r = r && consumeToken(b, HS_RIGHT_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name+
  private static boolean instvar_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instvar_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "instvar_2_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
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
  // PRAGMA_START onl "LANGUAGE" onl q_name (onl COMMA onl q_name?)* onl PRAGMA_END NEWLINE?
  public static boolean language_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "LANGUAGE");
    r = r && onl(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && language_pragma_5(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    r = r && language_pragma_8(b, l + 1);
    exit_section_(b, m, HS_LANGUAGE_PRAGMA, r);
    return r;
  }

  // (onl COMMA onl q_name?)*
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

  // onl COMMA onl q_name?
  private static boolean language_pragma_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && consumeToken(b, HS_COMMA);
    r = r && onl(b, l + 1);
    r = r && language_pragma_5_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name?
  private static boolean language_pragma_5_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma_5_0_3")) return false;
    q_name(b, l + 1);
    return true;
  }

  // NEWLINE?
  private static boolean language_pragma_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "language_pragma_8")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // type_signature | type_declaration | general_id+
  static boolean last_line_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "last_line_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_signature(b, l + 1);
    if (!r) r = type_declaration(b, l + 1);
    if (!r) r = last_line_expression_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // general_id+
  private static boolean last_line_expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "last_line_expression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "last_line_expression_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // type_signature | type_declaration | general_id+ nls
  static boolean line_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_signature(b, l + 1);
    if (!r) r = type_declaration(b, l + 1);
    if (!r) r = line_expression_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // general_id+ nls
  private static boolean line_expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = line_expression_2_0(b, l + 1);
    r = r && nls(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // general_id+
  private static boolean line_expression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_expression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_id(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!general_id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "line_expression_2_0", c)) break;
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
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_COLON_COLON);
    if (!r) r = q_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL | HEXADECIMAL | OCTAL | FLOAT | CHARACTER_LITERAL | STRING_LITERAL
  static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DECIMAL);
    if (!r) r = consumeToken(b, HS_HEXADECIMAL);
    if (!r) r = consumeToken(b, HS_OCTAL);
    if (!r) r = consumeToken(b, HS_FLOAT);
    if (!r) r = consumeToken(b, HS_CHARACTER_LITERAL);
    if (!r) r = consumeToken(b, HS_STRING_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl "MINIMAL" onl general_pragma_content onl PRAGMA_END
  public static boolean minimal_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "minimal_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "MINIMAL");
    r = r && onl(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_MINIMAL_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // (CON_ID DOT)* CON_ID
  public static boolean modid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modid")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HS_MODID, "<modid>");
    r = modid_0(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, HS_CON_ID);
    exit_section_(b, l, m, r, p, modid_recover_rule_parser_);
    return r || p;
  }

  // (CON_ID DOT)*
  private static boolean modid_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modid_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!modid_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modid_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // CON_ID DOT
  private static boolean modid_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modid_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CON_ID, HS_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DOT
  static boolean modid_recover_rule(PsiBuilder b, int l) {
    return consumeToken(b, HS_DOT);
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
  // MODULE modid onl deprecated_warn_pragma? onl (exports onl)? WHERE
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
    r = r && consumeToken(b, HS_WHERE);
    exit_section_(b, m, HS_MODULE_DECLARATION, r);
    return r;
  }

  // deprecated_warn_pragma?
  private static boolean module_declaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_declaration_3")) return false;
    deprecated_warn_pragma(b, l + 1);
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
  // q_name atype | newconstr_fielddecl
  public static boolean newconstr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_NEWCONSTR, "<newconstr>");
    r = newconstr_0(b, l + 1);
    if (!r) r = newconstr_fielddecl(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // q_name atype
  private static boolean newconstr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    r = r && atype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // q_name onls LEFT_BRACE? onls q_name onls COLON_COLON onls ttype onls RIGHT_BRACE?
  public static boolean newconstr_fielddecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newconstr_fielddecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_NEWCONSTR_FIELDDECL, "<newconstr fielddecl>");
    r = q_name(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && newconstr_fielddecl_2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && q_name(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_COLON_COLON);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && newconstr_fielddecl_10(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // NEWTYPE onls INSTANCE? onls ctype_pragma? onls (ccontext onls DOUBLE_RIGHT_ARROW)? onls simpletype onls EQUAL onls newconstr onls (DERIVING onls ttype)?
  public static boolean newtype_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration")) return false;
    if (!nextTokenIs(b, HS_NEWTYPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_NEWTYPE);
    r = r && onls(b, l + 1);
    r = r && newtype_declaration_2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && newtype_declaration_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && newtype_declaration_6(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_EQUAL);
    r = r && onls(b, l + 1);
    r = r && newconstr(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && newtype_declaration_14(b, l + 1);
    exit_section_(b, m, HS_NEWTYPE_DECLARATION, r);
    return r;
  }

  // INSTANCE?
  private static boolean newtype_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_2")) return false;
    consumeToken(b, HS_INSTANCE);
    return true;
  }

  // ctype_pragma?
  private static boolean newtype_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_4")) return false;
    ctype_pragma(b, l + 1);
    return true;
  }

  // (ccontext onls DOUBLE_RIGHT_ARROW)?
  private static boolean newtype_declaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_6")) return false;
    newtype_declaration_6_0(b, l + 1);
    return true;
  }

  // ccontext onls DOUBLE_RIGHT_ARROW
  private static boolean newtype_declaration_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ccontext(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DERIVING onls ttype)?
  private static boolean newtype_declaration_14(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_14")) return false;
    newtype_declaration_14_0(b, l + 1);
    return true;
  }

  // DERIVING onls ttype
  private static boolean newtype_declaration_14_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newtype_declaration_14_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DERIVING);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (&<<containsSpaces>> NEWLINE)+
  static boolean nls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nls")) return false;
    if (!nextTokenIs(b, HS_NEWLINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nls_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!nls_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "nls", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // &<<containsSpaces>> NEWLINE
  private static boolean nls_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nls_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nls_0_0(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  // &<<containsSpaces>>
  private static boolean nls_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nls_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = containsSpaces(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // (DIRECTIVE? NEWLINE)*
  static boolean onl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onl")) return false;
    int c = current_position_(b);
    while (true) {
      if (!onl_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "onl", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DIRECTIVE? NEWLINE
  private static boolean onl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onl_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl_0_0(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  // DIRECTIVE?
  private static boolean onl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onl_0_0")) return false;
    consumeToken(b, HS_DIRECTIVE);
    return true;
  }

  /* ********************************************************** */
  // (&<<containsSpaces>> NEWLINE | DIRECTIVE NEWLINE)*
  static boolean onls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onls")) return false;
    int c = current_position_(b);
    while (true) {
      if (!onls_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "onls", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // &<<containsSpaces>> NEWLINE | DIRECTIVE NEWLINE
  private static boolean onls_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onls_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onls_0_0(b, l + 1);
    if (!r) r = parseTokens(b, 0, HS_DIRECTIVE, HS_NEWLINE);
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

  /* ********************************************************** */
  // (&<<containsSpaces>> NEWLINE)?
  static boolean oonls(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "oonls")) return false;
    oonls_0(b, l + 1);
    return true;
  }

  // &<<containsSpaces>> NEWLINE
  private static boolean oonls_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "oonls_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = oonls_0_0(b, l + 1);
    r = r && consumeToken(b, HS_NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  // &<<containsSpaces>>
  private static boolean oonls_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "oonls_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = containsSpaces(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (VAR_ID ("-" (VAR_ID | TYPE))* | ("-" (VAR_ID | TYPE))+ | "-" CON_ID) (EQUAL literal)?
  public static boolean options_ghc_option(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_OPTIONS_GHC_OPTION, "<options ghc option>");
    r = options_ghc_option_0(b, l + 1);
    r = r && options_ghc_option_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // VAR_ID ("-" (VAR_ID | TYPE))* | ("-" (VAR_ID | TYPE))+ | "-" CON_ID
  private static boolean options_ghc_option_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = options_ghc_option_0_0(b, l + 1);
    if (!r) r = options_ghc_option_0_1(b, l + 1);
    if (!r) r = options_ghc_option_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VAR_ID ("-" (VAR_ID | TYPE))*
  private static boolean options_ghc_option_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    r = r && options_ghc_option_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("-" (VAR_ID | TYPE))*
  private static boolean options_ghc_option_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!options_ghc_option_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "options_ghc_option_0_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // "-" (VAR_ID | TYPE)
  private static boolean options_ghc_option_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "-");
    r = r && options_ghc_option_0_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VAR_ID | TYPE
  private static boolean options_ghc_option_0_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_0_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    if (!r) r = consumeToken(b, HS_TYPE);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("-" (VAR_ID | TYPE))+
  private static boolean options_ghc_option_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = options_ghc_option_0_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!options_ghc_option_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "options_ghc_option_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // "-" (VAR_ID | TYPE)
  private static boolean options_ghc_option_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "-");
    r = r && options_ghc_option_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VAR_ID | TYPE
  private static boolean options_ghc_option_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_VAR_ID);
    if (!r) r = consumeToken(b, HS_TYPE);
    exit_section_(b, m, null, r);
    return r;
  }

  // "-" CON_ID
  private static boolean options_ghc_option_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "-");
    r = r && consumeToken(b, HS_CON_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // (EQUAL literal)?
  private static boolean options_ghc_option_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_1")) return false;
    options_ghc_option_1_0(b, l + 1);
    return true;
  }

  // EQUAL literal
  private static boolean options_ghc_option_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_option_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_EQUAL);
    r = r && literal(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("OPTIONS_GHC" | "OPTIONS") onl options_ghc_option+ onl PRAGMA_END NEWLINE?
  public static boolean options_ghc_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && options_ghc_pragma_2(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && options_ghc_pragma_4(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    r = r && options_ghc_pragma_7(b, l + 1);
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

  // options_ghc_option+
  private static boolean options_ghc_pragma_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_pragma_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = options_ghc_option(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!options_ghc_option(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "options_ghc_pragma_4", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE?
  private static boolean options_ghc_pragma_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "options_ghc_pragma_7")) return false;
    consumeToken(b, HS_NEWLINE);
    return true;
  }

  /* ********************************************************** */
  // ann_pragma | deprecated_warn_pragma | noinline_pragma | inlinable_pragma | line_pragma | rules_pragma |
  //                                   specialize_pragma | inline_pragma | minimal_pragma | overlap_pragma | constant_folded_pragma | dummy_pragma
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
    if (!r) r = overlap_pragma(b, l + 1);
    if (!r) r = constant_folded_pragma(b, l + 1);
    if (!r) r = dummy_pragma(b, l + 1);
    exit_section_(b, m, HS_OTHER_PRAGMA, r);
    return r;
  }

  /* ********************************************************** */
  // PRAGMA_START onl ("OVERLAPPABLE" | "OVERLAPPING" | "OVERLAPS") onl PRAGMA_END
  public static boolean overlap_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "overlap_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && overlap_pragma_2(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_OVERLAP_PRAGMA, r);
    return r;
  }

  // "OVERLAPPABLE" | "OVERLAPPING" | "OVERLAPS"
  private static boolean overlap_pragma_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "overlap_pragma_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "OVERLAPPABLE");
    if (!r) r = consumeToken(b, "OVERLAPPING");
    if (!r) r = consumeToken(b, "OVERLAPS");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // onl SHEBANG_LINE? onl file_header? onl module_body
  static boolean program(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onl(b, l + 1);
    r = r && program_1(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && program_3(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && module_body(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SHEBANG_LINE?
  private static boolean program_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program_1")) return false;
    consumeToken(b, HS_SHEBANG_LINE);
    return true;
  }

  // file_header?
  private static boolean program_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program_3")) return false;
    file_header(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // q_con_qualifier4 DOT conid | q_con_qualifier3 DOT conid | q_con_qualifier2 DOT conid | q_con_qualifier1 DOT conid
  public static boolean q_con(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con_0(b, l + 1);
    if (!r) r = q_con_1(b, l + 1);
    if (!r) r = q_con_2(b, l + 1);
    if (!r) r = q_con_3(b, l + 1);
    exit_section_(b, m, HS_Q_CON, r);
    return r;
  }

  // q_con_qualifier4 DOT conid
  private static boolean q_con_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con_qualifier4(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && conid(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_con_qualifier3 DOT conid
  private static boolean q_con_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con_qualifier3(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && conid(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_con_qualifier2 DOT conid
  private static boolean q_con_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con_qualifier2(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && conid(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_con_qualifier1 DOT conid
  private static boolean q_con_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_con_qualifier1(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && conid(b, l + 1);
    exit_section_(b, m, null, r);
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
  // CON_ID
  public static boolean q_con_qualifier1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_qualifier1")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CON_ID);
    exit_section_(b, m, HS_Q_CON_QUALIFIER_1, r);
    return r;
  }

  /* ********************************************************** */
  // CON_ID DOT CON_ID
  public static boolean q_con_qualifier2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_qualifier2")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CON_ID, HS_DOT, HS_CON_ID);
    exit_section_(b, m, HS_Q_CON_QUALIFIER_2, r);
    return r;
  }

  /* ********************************************************** */
  // CON_ID DOT CON_ID DOT CON_ID
  public static boolean q_con_qualifier3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_qualifier3")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CON_ID, HS_DOT, HS_CON_ID, HS_DOT, HS_CON_ID);
    exit_section_(b, m, HS_Q_CON_QUALIFIER_3, r);
    return r;
  }

  /* ********************************************************** */
  // CON_ID DOT CON_ID DOT CON_ID DOT CON_ID
  public static boolean q_con_qualifier4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_con_qualifier4")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_CON_ID, HS_DOT, HS_CON_ID, HS_DOT, HS_CON_ID, HS_DOT, HS_CON_ID);
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
    int c = current_position_(b);
    while (true) {
      if (!q_names_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "q_names_1", c)) break;
      c = current_position_(b);
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
  // qualifier DOT (varid | consym | DOT? varsym) | q_con
  public static boolean q_var_con(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_var_con")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_var_con_0(b, l + 1);
    if (!r) r = q_con(b, l + 1);
    exit_section_(b, m, HS_Q_VAR_CON, r);
    return r;
  }

  // qualifier DOT (varid | consym | DOT? varsym)
  private static boolean q_var_con_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_var_con_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifier(b, l + 1);
    r = r && consumeToken(b, HS_DOT);
    r = r && q_var_con_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // varid | consym | DOT? varsym
  private static boolean q_var_con_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_var_con_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varid(b, l + 1);
    if (!r) r = consym(b, l + 1);
    if (!r) r = q_var_con_0_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT? varsym
  private static boolean q_var_con_0_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_var_con_0_2_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_var_con_0_2_2_0(b, l + 1);
    r = r && varsym(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT?
  private static boolean q_var_con_0_2_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "q_var_con_0_2_2_0")) return false;
    consumeToken(b, HS_DOT);
    return true;
  }

  /* ********************************************************** */
  // CON_ID (DOT CON_ID)*
  public static boolean qualifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier")) return false;
    if (!nextTokenIs(b, HS_CON_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_CON_ID);
    r = r && qualifier_1(b, l + 1);
    exit_section_(b, m, HS_QUALIFIER, r);
    return r;
  }

  // (DOT CON_ID)*
  private static boolean qualifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qualifier_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qualifier_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DOT CON_ID
  private static boolean qualifier_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifier_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HS_DOT, HS_CON_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASE | CLASS | DATA | DEFAULT | DERIVING | DO | ELSE | IF | IMPORT | IN | INFIX | INFIXL | INFIXR | INSTANCE | LET | MODULE | NEWTYPE | OF | THEN | TYPE | WHERE | UNDERSCORE
  public static boolean reserved_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "reserved_id")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_RESERVED_ID, "<reserved id>");
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
    if (!r) r = consumeToken(b, HS_INFIX);
    if (!r) r = consumeToken(b, HS_INFIXL);
    if (!r) r = consumeToken(b, HS_INFIXR);
    if (!r) r = consumeToken(b, HS_INSTANCE);
    if (!r) r = consumeToken(b, HS_LET);
    if (!r) r = consumeToken(b, HS_MODULE);
    if (!r) r = consumeToken(b, HS_NEWTYPE);
    if (!r) r = consumeToken(b, HS_OF);
    if (!r) r = consumeToken(b, HS_THEN);
    if (!r) r = consumeToken(b, HS_TYPE);
    if (!r) r = consumeToken(b, HS_WHERE);
    if (!r) r = consumeToken(b, HS_UNDERSCORE);
    exit_section_(b, l, m, r, false, null);
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
  // PRAGMA_START onl "SCC" onl general_pragma_content onl PRAGMA_END
  public static boolean scc_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scc_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_PRAGMA_START);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, "SCC");
    r = r && onl(b, l + 1);
    r = r && general_pragma_content(b, l + 1);
    r = r && onl(b, l + 1);
    r = r && consumeToken(b, HS_PRAGMA_END);
    exit_section_(b, m, HS_SCC_PRAGMA, r);
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
    int c = current_position_(b);
    while (true) {
      if (!scontext_1_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scontext_1_5", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_1_0", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_1_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name*
  private static boolean simpleclass_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_1_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_1_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // q_name+
  private static boolean simpleclass_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleclass_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleclass_2", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!simpletype_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_2_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // q_name*
  private static boolean simpletype_2_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_2_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_2_6", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_3_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (LEFT_PAREN type_signature RIGHT_PAREN)+
  private static boolean simpletype_3_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpletype_3_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpletype_3_4_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!simpletype_3_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_3_4", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_3_6", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpletype_6_2", c)) break;
      c = current_position_(b);
    }
    return true;
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
  // btype | q_name atype
  public static boolean sub_constr2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sub_constr2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_SUB_CONSTR_2, "<sub constr 2>");
    r = btype(b, l + 1);
    if (!r) r = sub_constr2_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // q_name atype
  private static boolean sub_constr2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sub_constr2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
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
  //                                   foreign_declaration | type_family_declaration | deriving_declaration | type_instance_declaration | type_signature |
  //                                   other_pragma | expression | cfiles_pragma | fixity_declaration | DIRECTIVE
  public static boolean top_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_TOP_DECLARATION, "<top declaration>");
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
    if (!r) r = other_pragma(b, l + 1);
    if (!r) r = expression(b, l + 1);
    if (!r) r = cfiles_pragma(b, l + 1);
    if (!r) r = fixity_declaration(b, l + 1);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (top_declaration (NEWLINE | DIRECTIVE)+)* top_declaration?
  static boolean top_declarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = top_declarations_0(b, l + 1);
    r = r && top_declarations_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (top_declaration (NEWLINE | DIRECTIVE)+)*
  private static boolean top_declarations_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!top_declarations_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "top_declarations_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // top_declaration (NEWLINE | DIRECTIVE)+
  private static boolean top_declarations_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = top_declaration(b, l + 1);
    r = r && top_declarations_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (NEWLINE | DIRECTIVE)+
  private static boolean top_declarations_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = top_declarations_0_0_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!top_declarations_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "top_declarations_0_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // NEWLINE | DIRECTIVE
  private static boolean top_declarations_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_declarations_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_NEWLINE);
    if (!r) r = consumeToken(b, HS_DIRECTIVE);
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
  // varsym | varsym? btype (oonls RIGHT_ARROW oonls ttype)* | list_type q_name* | LEFT_PAREN RIGHT_ARROW RIGHT_PAREN
  public static boolean ttype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, HS_TTYPE, "<ttype>");
    r = varsym(b, l + 1);
    if (!r) r = ttype_1(b, l + 1);
    if (!r) r = ttype_2(b, l + 1);
    if (!r) r = parseTokens(b, 0, HS_LEFT_PAREN, HS_RIGHT_ARROW, HS_RIGHT_PAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // varsym? btype (oonls RIGHT_ARROW oonls ttype)*
  private static boolean ttype_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype_1_0(b, l + 1);
    r = r && btype(b, l + 1);
    r = r && ttype_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // varsym?
  private static boolean ttype_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_1_0")) return false;
    varsym(b, l + 1);
    return true;
  }

  // (oonls RIGHT_ARROW oonls ttype)*
  private static boolean ttype_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_1_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ttype_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ttype_1_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // oonls RIGHT_ARROW oonls ttype
  private static boolean ttype_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_1_2_0")) return false;
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
  private static boolean ttype_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = list_type(b, l + 1);
    r = r && ttype_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // q_name*
  private static boolean ttype_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ttype_2_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!q_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ttype_2_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // TYPE onls simpletype onls COLON_COLON onls ttype |
  //                                   TYPE onls simpletype onls (EQUAL | WHERE) onls (ttype | type_signature) (DOUBLE_RIGHT_ARROW ttype)? |
  //                                   TYPE onls simpletype onls EQUAL onls expression |
  //                                   TYPE onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls EQUAL onls ttype |
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

  // TYPE onls simpletype onls (EQUAL | WHERE) onls (ttype | type_signature) (DOUBLE_RIGHT_ARROW ttype)?
  private static boolean type_declaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && onls(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_declaration_1_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_declaration_1_6(b, l + 1);
    r = r && type_declaration_1_7(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQUAL | WHERE
  private static boolean type_declaration_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_EQUAL);
    if (!r) r = consumeToken(b, HS_WHERE);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype | type_signature
  private static boolean type_declaration_1_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_1_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ttype(b, l + 1);
    if (!r) r = type_signature(b, l + 1);
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

  // TYPE onls simpletype onls EQUAL onls expression
  private static boolean type_declaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_2")) return false;
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

  // TYPE onls simpletype onls (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)* onls EQUAL onls ttype
  private static boolean type_declaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && onls(b, l + 1);
    r = r && simpletype(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_declaration_3_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_EQUAL);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PAREN onls kind_signature onls RIGHT_PAREN)*
  private static boolean type_declaration_3_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_3_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!type_declaration_3_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "type_declaration_3_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // LEFT_PAREN onls kind_signature onls RIGHT_PAREN
  private static boolean type_declaration_3_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_3_4_0")) return false;
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

  // TYPE onls simpletype
  private static boolean type_declaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_declaration_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE);
    r = r && onls(b, l + 1);
    r = r && simpletype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TYPE_FAMILY onls type_family_type onls WHERE? onls expression?
  public static boolean type_family_declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_declaration")) return false;
    if (!nextTokenIs(b, HS_TYPE_FAMILY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_TYPE_FAMILY);
    r = r && onls(b, l + 1);
    r = r && type_family_type(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_family_declaration_4(b, l + 1);
    r = r && onls(b, l + 1);
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
  // (q_name | LEFT_PAREN? q_names COLON_COLON? onls (ccontext DOUBLE_RIGHT_ARROW)? onls ttype? RIGHT_PAREN?)+ (COLON_COLON onls ttype)?
  public static boolean type_family_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_TYPE_FAMILY_TYPE, "<type family type>");
    r = type_family_type_0(b, l + 1);
    r = r && type_family_type_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (q_name | LEFT_PAREN? q_names COLON_COLON? onls (ccontext DOUBLE_RIGHT_ARROW)? onls ttype? RIGHT_PAREN?)+
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

  // q_name | LEFT_PAREN? q_names COLON_COLON? onls (ccontext DOUBLE_RIGHT_ARROW)? onls ttype? RIGHT_PAREN?
  private static boolean type_family_type_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_name(b, l + 1);
    if (!r) r = type_family_type_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PAREN? q_names COLON_COLON? onls (ccontext DOUBLE_RIGHT_ARROW)? onls ttype? RIGHT_PAREN?
  private static boolean type_family_type_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_family_type_0_0_1_0(b, l + 1);
    r = r && q_names(b, l + 1);
    r = r && type_family_type_0_0_1_2(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_family_type_0_0_1_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && type_family_type_0_0_1_6(b, l + 1);
    r = r && type_family_type_0_0_1_7(b, l + 1);
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

  // (ccontext DOUBLE_RIGHT_ARROW)?
  private static boolean type_family_type_0_0_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_1_4")) return false;
    type_family_type_0_0_1_4_0(b, l + 1);
    return true;
  }

  // ccontext DOUBLE_RIGHT_ARROW
  private static boolean type_family_type_0_0_1_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_1_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ccontext(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
    return r;
  }

  // ttype?
  private static boolean type_family_type_0_0_1_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_1_6")) return false;
    ttype(b, l + 1);
    return true;
  }

  // RIGHT_PAREN?
  private static boolean type_family_type_0_0_1_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_family_type_0_0_1_7")) return false;
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
  // q_names onls COLON_COLON onls (ccontext DOUBLE_RIGHT_ARROW)* onls ttype |
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

  // q_names onls COLON_COLON onls (ccontext DOUBLE_RIGHT_ARROW)* onls ttype
  private static boolean type_signature_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = q_names(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && consumeToken(b, HS_COLON_COLON);
    r = r && onls(b, l + 1);
    r = r && type_signature_0_4(b, l + 1);
    r = r && onls(b, l + 1);
    r = r && ttype(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ccontext DOUBLE_RIGHT_ARROW)*
  private static boolean type_signature_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_0_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!type_signature_0_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "type_signature_0_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ccontext DOUBLE_RIGHT_ARROW
  private static boolean type_signature_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_signature_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ccontext(b, l + 1);
    r = r && consumeToken(b, HS_DOUBLE_RIGHT_ARROW);
    exit_section_(b, m, null, r);
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
  // unpack_pragma | nounpack_pragma | ctype_pragma
  public static boolean unpack_nounpack_pragma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unpack_nounpack_pragma")) return false;
    if (!nextTokenIs(b, HS_PRAGMA_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unpack_pragma(b, l + 1);
    if (!r) r = nounpack_pragma(b, l + 1);
    if (!r) r = ctype_pragma(b, l + 1);
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
  // varid | LEFT_PAREN varsym RIGHT_PAREN
  public static boolean var(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var")) return false;
    if (!nextTokenIs(b, "<var>", HS_LEFT_PAREN, HS_VAR_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_VAR, "<var>");
    r = varid(b, l + 1);
    if (!r) r = var_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_PAREN varsym RIGHT_PAREN
  private static boolean var_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_LEFT_PAREN);
    r = r && varsym(b, l + 1);
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
  // varsym | BACKQUOTE varid BACKQUOTE
  public static boolean varop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varop")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_VAROP, "<varop>");
    r = varsym(b, l + 1);
    if (!r) r = varop_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BACKQUOTE varid BACKQUOTE
  private static boolean varop_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varop_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_BACKQUOTE);
    r = r && varid(b, l + 1);
    r = r && consumeToken(b, HS_BACKQUOTE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VARSYM_ID | TILDE | (DOT | DOT_DOT) VARSYM_ID | (DOT | DOT_DOT) CONSYM_ID | (DOT | DOT_DOT) EQUAL | DOT
  public static boolean varsym(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HS_VARSYM, "<varsym>");
    r = consumeToken(b, HS_VARSYM_ID);
    if (!r) r = consumeToken(b, HS_TILDE);
    if (!r) r = varsym_2(b, l + 1);
    if (!r) r = varsym_3(b, l + 1);
    if (!r) r = varsym_4(b, l + 1);
    if (!r) r = consumeToken(b, HS_DOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (DOT | DOT_DOT) VARSYM_ID
  private static boolean varsym_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varsym_2_0(b, l + 1);
    r = r && consumeToken(b, HS_VARSYM_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT | DOT_DOT
  private static boolean varsym_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOT);
    if (!r) r = consumeToken(b, HS_DOT_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOT | DOT_DOT) CONSYM_ID
  private static boolean varsym_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varsym_3_0(b, l + 1);
    r = r && consumeToken(b, HS_CONSYM_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT | DOT_DOT
  private static boolean varsym_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOT);
    if (!r) r = consumeToken(b, HS_DOT_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOT | DOT_DOT) EQUAL
  private static boolean varsym_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varsym_4_0(b, l + 1);
    r = r && consumeToken(b, HS_EQUAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT | DOT_DOT
  private static boolean varsym_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varsym_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HS_DOT);
    if (!r) r = consumeToken(b, HS_DOT_DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  final static Parser modid_recover_rule_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return modid_recover_rule(b, l + 1);
    }
  };
}
