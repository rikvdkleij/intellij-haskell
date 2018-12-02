// This is a generated file. Not intended for manual editing.
package intellij.haskell.alex.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static intellij.haskell.alex.lang.psi.AlexTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class AlexParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, null);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    if (type == ALEX_DECLARATION) {
      result = declaration(builder, 0);
    }
    else if (type == ALEX_DECLARATIONS_SECTION) {
      result = declarations_section(builder, 0);
    }
    else if (type == ALEX_IDENTIFIER) {
      result = identifier(builder, 0);
    }
    else if (type == ALEX_REGEX) {
      result = regex(builder, 0);
    }
    else if (type == ALEX_REGEX_PART) {
      result = regex_part(builder, 0);
    }
    else if (type == ALEX_RULE_DECLARATION) {
      result = rule_declaration(builder, 0);
    }
    else if (type == ALEX_STATEFUL_TOKENS_RULE) {
      result = stateful_tokens_rule(builder, 0);
    }
    else if (type == ALEX_STATELESS_TOKENS_RULE) {
      result = stateless_tokens_rule(builder, 0);
    }
    else if (type == ALEX_TOKEN_SET_DECLARATION) {
      result = token_set_declaration(builder, 0);
    }
    else if (type == ALEX_TOKENS_RULE) {
      result = tokens_rule(builder, 0);
    }
    else if (type == ALEX_TOKENS_SECTION) {
      result = tokens_section(builder, 0);
    }
    else if (type == ALEX_TOP_MODULE_SECTION) {
      result = top_module_section(builder, 0);
    }
    else if (type == ALEX_USER_CODE_SECTION) {
      result = user_code_section(builder, 0);
    }
    else if (type == ALEX_WRAPPER_TYPE) {
      result = wrapper_type(builder, 0);
    }
    else {
      result = parse_root_(type, builder, 0);
    }
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    return alex_file(builder, level + 1);
  }

  /* ********************************************************** */
  // (top_module_section endOfLine+)
  //  (wrapper_type endOfLine+)?
  //  (declarations_section)
  //  (tokens_section endOfLine+)
  //  (user_code_section endOfLine*)
  static boolean alex_file(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file")) return false;
    if (!nextTokenIs(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = alex_file_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, alex_file_1(builder, level + 1));
    result = pinned && report_error_(builder, alex_file_2(builder, level + 1)) && result;
    result = pinned && report_error_(builder, alex_file_3(builder, level + 1)) && result;
    result = pinned && alex_file_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // top_module_section endOfLine+
  private static boolean alex_file_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = top_module_section(builder, level + 1);
    result = result && alex_file_0_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // endOfLine+
  private static boolean alex_file_0_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_0_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = endOfLine(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!endOfLine(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "alex_file_0_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  // (wrapper_type endOfLine+)?
  private static boolean alex_file_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_1")) return false;
    alex_file_1_0(builder, level + 1);
    return true;
  }

  // wrapper_type endOfLine+
  private static boolean alex_file_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = wrapper_type(builder, level + 1);
    result = result && alex_file_1_0_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // endOfLine+
  private static boolean alex_file_1_0_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_1_0_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = endOfLine(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!endOfLine(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "alex_file_1_0_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  // (declarations_section)
  private static boolean alex_file_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_2")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = declarations_section(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // tokens_section endOfLine+
  private static boolean alex_file_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_3")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = tokens_section(builder, level + 1);
    result = result && alex_file_3_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // endOfLine+
  private static boolean alex_file_3_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_3_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = endOfLine(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!endOfLine(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "alex_file_3_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  // user_code_section endOfLine*
  private static boolean alex_file_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_4")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = user_code_section(builder, level + 1);
    result = result && alex_file_4_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // endOfLine*
  private static boolean alex_file_4_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alex_file_4_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!endOfLine(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "alex_file_4_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // token_set_declaration | rule_declaration
  public static boolean declaration(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "declaration")) return false;
    if (!nextTokenIs(builder, "<declaration>", ALEX_DOLLAR_AND_IDENTIFIER, ALEX_EMAIL_AND_IDENTIFIER)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_DECLARATION, "<declaration>");
    result = token_set_declaration(builder, level + 1);
    if (!result) result = rule_declaration(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // (declaration endOfLine)*
  public static boolean declarations_section(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "declarations_section")) return false;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_DECLARATIONS_SECTION, "<declarations section>");
    while (true) {
      int pos = current_position_(builder);
      if (!declarations_section_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "declarations_section", pos)) break;
    }
    exit_section_(builder, level, marker, true, false, null);
    return true;
  }

  // declaration endOfLine
  private static boolean declarations_section_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "declarations_section_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = declaration(builder, level + 1);
    result = result && endOfLine(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // EOL+
  static boolean endOfLine(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "endOfLine")) return false;
    if (!nextTokenIs(builder, ALEX_EOL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, ALEX_EOL);
    while (result) {
      int pos = current_position_(builder);
      if (!consumeToken(builder, ALEX_EOL)) break;
      if (!empty_element_parsed_guard_(builder, "endOfLine", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // HASKELL_IDENTIFIER
  public static boolean identifier(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "identifier")) return false;
    if (!nextTokenIs(builder, ALEX_HASKELL_IDENTIFIER)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, ALEX_HASKELL_IDENTIFIER);
    exit_section_(builder, marker, ALEX_IDENTIFIER, result);
    return result;
  }

  /* ********************************************************** */
  // (regex_part | DOLLAR_AND_IDENTIFIER)*
  public static boolean regex(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "regex")) return false;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_REGEX, "<regex>");
    while (true) {
      int pos = current_position_(builder);
      if (!regex_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "regex", pos)) break;
    }
    exit_section_(builder, level, marker, true, false, null);
    return true;
  }

  // regex_part | DOLLAR_AND_IDENTIFIER
  private static boolean regex_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "regex_0")) return false;
    boolean result;
    result = regex_part(builder, level + 1);
    if (!result) result = consumeToken(builder, ALEX_DOLLAR_AND_IDENTIFIER);
    return result;
  }

  /* ********************************************************** */
  // REGEX_PART_TOKEN
  public static boolean regex_part(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "regex_part")) return false;
    if (!nextTokenIs(builder, ALEX_REGEX_PART_TOKEN)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, ALEX_REGEX_PART_TOKEN);
    exit_section_(builder, marker, ALEX_REGEX_PART, result);
    return result;
  }

  /* ********************************************************** */
  // EMAIL_AND_IDENTIFIER EQUAL regex
  public static boolean rule_declaration(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "rule_declaration")) return false;
    if (!nextTokenIs(builder, ALEX_EMAIL_AND_IDENTIFIER)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_RULE_DECLARATION, null);
    result = consumeTokens(builder, 1, ALEX_EMAIL_AND_IDENTIFIER, ALEX_EQUAL);
    pinned = result; // pin = 1
    result = result && regex(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // STATEFUL_TOKENS_RULE_START
  //  identifier
  //  STATEFUL_TOKENS_RULE_END endOfLine?
  //  tokens_rule_body endOfLine?
  public static boolean stateful_tokens_rule(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateful_tokens_rule")) return false;
    if (!nextTokenIs(builder, ALEX_STATEFUL_TOKENS_RULE_START)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_STATEFUL_TOKENS_RULE, null);
    result = consumeToken(builder, ALEX_STATEFUL_TOKENS_RULE_START);
    pinned = result; // pin = 1
    result = result && report_error_(builder, identifier(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, ALEX_STATEFUL_TOKENS_RULE_END)) && result;
    result = pinned && report_error_(builder, stateful_tokens_rule_3(builder, level + 1)) && result;
    result = pinned && report_error_(builder, tokens_rule_body(builder, level + 1)) && result;
    result = pinned && stateful_tokens_rule_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // endOfLine?
  private static boolean stateful_tokens_rule_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateful_tokens_rule_3")) return false;
    endOfLine(builder, level + 1);
    return true;
  }

  // endOfLine?
  private static boolean stateful_tokens_rule_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateful_tokens_rule_5")) return false;
    endOfLine(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // EMAIL_AND_IDENTIFIER
  //  SOMETHING_IS_GONNA_HAPPEN
  //  (SOMETHING_IS_HAPPENING | endOfLine)*
  //  SOMETHING_HAS_ALREADY_HAPPENED
  public static boolean stateless_tokens_rule(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateless_tokens_rule")) return false;
    if (!nextTokenIs(builder, ALEX_EMAIL_AND_IDENTIFIER)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_STATELESS_TOKENS_RULE, null);
    result = consumeTokens(builder, 1, ALEX_EMAIL_AND_IDENTIFIER, ALEX_SOMETHING_IS_GONNA_HAPPEN);
    pinned = result; // pin = 1
    result = result && report_error_(builder, stateless_tokens_rule_2(builder, level + 1));
    result = pinned && consumeToken(builder, ALEX_SOMETHING_HAS_ALREADY_HAPPENED) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (SOMETHING_IS_HAPPENING | endOfLine)*
  private static boolean stateless_tokens_rule_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateless_tokens_rule_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!stateless_tokens_rule_2_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "stateless_tokens_rule_2", pos)) break;
    }
    return true;
  }

  // SOMETHING_IS_HAPPENING | endOfLine
  private static boolean stateless_tokens_rule_2_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stateless_tokens_rule_2_0")) return false;
    boolean result;
    result = consumeToken(builder, ALEX_SOMETHING_IS_HAPPENING);
    if (!result) result = endOfLine(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // DOLLAR_AND_IDENTIFIER EQUAL regex
  public static boolean token_set_declaration(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "token_set_declaration")) return false;
    if (!nextTokenIs(builder, ALEX_DOLLAR_AND_IDENTIFIER)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_TOKEN_SET_DECLARATION, null);
    result = consumeTokens(builder, 1, ALEX_DOLLAR_AND_IDENTIFIER, ALEX_EQUAL);
    pinned = result; // pin = 1
    result = result && regex(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // stateful_tokens_rule | stateless_tokens_rule
  public static boolean tokens_rule(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule")) return false;
    if (!nextTokenIs(builder, "<tokens rule>", ALEX_EMAIL_AND_IDENTIFIER, ALEX_STATEFUL_TOKENS_RULE_START)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_TOKENS_RULE, "<tokens rule>");
    result = stateful_tokens_rule(builder, level + 1);
    if (!result) result = stateless_tokens_rule(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // SEMICOLON |
  //  SOMETHING_IS_GONNA_HAPPEN endOfLine?
  //  (stateless_tokens_rule | endOfLine)*
  //  SOMETHING_HAS_ALREADY_HAPPENED
  static boolean tokens_rule_body(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule_body")) return false;
    if (!nextTokenIs(builder, "", ALEX_SEMICOLON, ALEX_SOMETHING_IS_GONNA_HAPPEN)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, ALEX_SEMICOLON);
    if (!result) result = tokens_rule_body_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // SOMETHING_IS_GONNA_HAPPEN endOfLine?
  //  (stateless_tokens_rule | endOfLine)*
  //  SOMETHING_HAS_ALREADY_HAPPENED
  private static boolean tokens_rule_body_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule_body_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN);
    result = result && tokens_rule_body_1_1(builder, level + 1);
    result = result && tokens_rule_body_1_2(builder, level + 1);
    result = result && consumeToken(builder, ALEX_SOMETHING_HAS_ALREADY_HAPPENED);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // endOfLine?
  private static boolean tokens_rule_body_1_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule_body_1_1")) return false;
    endOfLine(builder, level + 1);
    return true;
  }

  // (stateless_tokens_rule | endOfLine)*
  private static boolean tokens_rule_body_1_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule_body_1_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!tokens_rule_body_1_2_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "tokens_rule_body_1_2", pos)) break;
    }
    return true;
  }

  // stateless_tokens_rule | endOfLine
  private static boolean tokens_rule_body_1_2_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_rule_body_1_2_0")) return false;
    boolean result;
    result = stateless_tokens_rule(builder, level + 1);
    if (!result) result = endOfLine(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // TOKENS A_SYMBOL_FOLLOWED_BY_TOKENS endOfLine
  //  tokens_rule*
  public static boolean tokens_section(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_section")) return false;
    if (!nextTokenIs(builder, ALEX_TOKENS)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_TOKENS_SECTION, null);
    result = consumeTokens(builder, 1, ALEX_TOKENS, ALEX_A_SYMBOL_FOLLOWED_BY_TOKENS);
    pinned = result; // pin = 1
    result = result && report_error_(builder, endOfLine(builder, level + 1));
    result = pinned && tokens_section_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // tokens_rule*
  private static boolean tokens_section_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "tokens_section_3")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!tokens_rule(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "tokens_section_3", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // SOMETHING_IS_GONNA_HAPPEN
  //  (SOMETHING_IS_HAPPENING | endOfLine)*
  //  SOMETHING_HAS_ALREADY_HAPPENED
  public static boolean top_module_section(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "top_module_section")) return false;
    if (!nextTokenIs(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_TOP_MODULE_SECTION, null);
    result = consumeToken(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN);
    pinned = result; // pin = 1
    result = result && report_error_(builder, top_module_section_1(builder, level + 1));
    result = pinned && consumeToken(builder, ALEX_SOMETHING_HAS_ALREADY_HAPPENED) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (SOMETHING_IS_HAPPENING | endOfLine)*
  private static boolean top_module_section_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "top_module_section_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!top_module_section_1_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "top_module_section_1", pos)) break;
    }
    return true;
  }

  // SOMETHING_IS_HAPPENING | endOfLine
  private static boolean top_module_section_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "top_module_section_1_0")) return false;
    boolean result;
    result = consumeToken(builder, ALEX_SOMETHING_IS_HAPPENING);
    if (!result) result = endOfLine(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // SOMETHING_IS_GONNA_HAPPEN
  //  (SOMETHING_IS_HAPPENING | endOfLine)*
  //  SOMETHING_HAS_ALREADY_HAPPENED
  public static boolean user_code_section(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "user_code_section")) return false;
    if (!nextTokenIs(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_USER_CODE_SECTION, null);
    result = consumeToken(builder, ALEX_SOMETHING_IS_GONNA_HAPPEN);
    pinned = result; // pin = 1
    result = result && report_error_(builder, user_code_section_1(builder, level + 1));
    result = pinned && consumeToken(builder, ALEX_SOMETHING_HAS_ALREADY_HAPPENED) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (SOMETHING_IS_HAPPENING | endOfLine)*
  private static boolean user_code_section_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "user_code_section_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!user_code_section_1_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "user_code_section_1", pos)) break;
    }
    return true;
  }

  // SOMETHING_IS_HAPPENING | endOfLine
  private static boolean user_code_section_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "user_code_section_1_0")) return false;
    boolean result;
    result = consumeToken(builder, ALEX_SOMETHING_IS_HAPPENING);
    if (!result) result = endOfLine(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // WRAPPER_TYPE_IS_GONNA_BE_HERE STRING
  public static boolean wrapper_type(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "wrapper_type")) return false;
    if (!nextTokenIs(builder, ALEX_WRAPPER_TYPE_IS_GONNA_BE_HERE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ALEX_WRAPPER_TYPE, null);
    result = consumeTokens(builder, 1, ALEX_WRAPPER_TYPE_IS_GONNA_BE_HERE, ALEX_STRING);
    pinned = result; // pin = 1
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

}
