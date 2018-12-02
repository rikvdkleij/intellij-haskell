// This is a generated file. Not intended for manual editing.
package intellij.haskell.alex.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import intellij.haskell.alex.lang.psi.impl.*;

public interface AlexTypes {

  IElementType ALEX_DECLARATION = new AlexTokenType("ALEX_DECLARATION");
  IElementType ALEX_DECLARATIONS_SECTION = new AlexTokenType("ALEX_DECLARATIONS_SECTION");
  IElementType ALEX_IDENTIFIER = new AlexTokenType("ALEX_IDENTIFIER");
  IElementType ALEX_REGEX = new AlexTokenType("ALEX_REGEX");
  IElementType ALEX_REGEX_PART = new AlexTokenType("ALEX_REGEX_PART");
  IElementType ALEX_RULE_DECLARATION = new AlexTokenType("ALEX_RULE_DECLARATION");
  IElementType ALEX_RULE_DESCRIPTION = new AlexTokenType("ALEX_RULE_DESCRIPTION");
  IElementType ALEX_STATEFUL_TOKENS_RULE = new AlexTokenType("ALEX_STATEFUL_TOKENS_RULE");
  IElementType ALEX_STATELESS_TOKENS_RULE = new AlexTokenType("ALEX_STATELESS_TOKENS_RULE");
  IElementType ALEX_TOKENS_RULE = new AlexTokenType("ALEX_TOKENS_RULE");
  IElementType ALEX_TOKENS_SECTION = new AlexTokenType("ALEX_TOKENS_SECTION");
  IElementType ALEX_TOKEN_SET_DECLARATION = new AlexTokenType("ALEX_TOKEN_SET_DECLARATION");
  IElementType ALEX_TOP_MODULE_SECTION = new AlexTokenType("ALEX_TOP_MODULE_SECTION");
  IElementType ALEX_USER_CODE_SECTION = new AlexTokenType("ALEX_USER_CODE_SECTION");
  IElementType ALEX_WRAPPER_TYPE = new AlexTokenType("ALEX_WRAPPER_TYPE");

  IElementType ALEX_A_SYMBOL_FOLLOWED_BY_TOKENS = new AlexTokenType("A_SYMBOL_FOLLOWED_BY_TOKENS");
  IElementType ALEX_DOLLAR_AND_IDENTIFIER = new AlexTokenType("DOLLAR_AND_IDENTIFIER");
  IElementType ALEX_EMAIL_AND_IDENTIFIER = new AlexTokenType("EMAIL_AND_IDENTIFIER");
  IElementType ALEX_EOL = new AlexTokenType("EOL");
  IElementType ALEX_EQUAL = new AlexTokenType("EQUAL");
  IElementType ALEX_HASKELL_IDENTIFIER = new AlexTokenType("HASKELL_IDENTIFIER");
  IElementType ALEX_LEFT_LISP = new AlexTokenType("LEFT_LISP");
  IElementType ALEX_PUBLIC_REGEX = new AlexTokenType("PUBLIC_REGEX");
  IElementType ALEX_REGEX_PART_TOKEN = new AlexTokenType("REGEX_PART_TOKEN");
  IElementType ALEX_RIGHT_LISP = new AlexTokenType("RIGHT_LISP");
  IElementType ALEX_SEMICOLON = new AlexTokenType("SEMICOLON");
  IElementType ALEX_SOMETHING_HAS_ALREADY_HAPPENED = new AlexTokenType("SOMETHING_HAS_ALREADY_HAPPENED");
  IElementType ALEX_SOMETHING_IS_GONNA_HAPPEN = new AlexTokenType("SOMETHING_IS_GONNA_HAPPEN");
  IElementType ALEX_SOMETHING_IS_HAPPENING = new AlexTokenType("SOMETHING_IS_HAPPENING");
  IElementType ALEX_STATEFUL_TOKENS_RULE_END = new AlexTokenType("STATEFUL_TOKENS_RULE_END");
  IElementType ALEX_STATEFUL_TOKENS_RULE_START = new AlexTokenType("STATEFUL_TOKENS_RULE_START");
  IElementType ALEX_STRING = new AlexTokenType("STRING");
  IElementType ALEX_WRAPPER_TYPE_IS_GONNA_BE_HERE = new AlexTokenType("WRAPPER_TYPE_IS_GONNA_BE_HERE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ALEX_DECLARATION) {
        return new AlexDeclarationImpl(node);
      }
      else if (type == ALEX_DECLARATIONS_SECTION) {
        return new AlexDeclarationsSectionImpl(node);
      }
      else if (type == ALEX_IDENTIFIER) {
        return new AlexIdentifierImpl(node);
      }
      else if (type == ALEX_REGEX) {
        return new AlexRegexImpl(node);
      }
      else if (type == ALEX_REGEX_PART) {
        return new AlexRegexPartImpl(node);
      }
      else if (type == ALEX_RULE_DECLARATION) {
        return new AlexRuleDeclarationImpl(node);
      }
      else if (type == ALEX_RULE_DESCRIPTION) {
        return new AlexRuleDescriptionImpl(node);
      }
      else if (type == ALEX_STATEFUL_TOKENS_RULE) {
        return new AlexStatefulTokensRuleImpl(node);
      }
      else if (type == ALEX_STATELESS_TOKENS_RULE) {
        return new AlexStatelessTokensRuleImpl(node);
      }
      else if (type == ALEX_TOKENS_RULE) {
        return new AlexTokensRuleImpl(node);
      }
      else if (type == ALEX_TOKENS_SECTION) {
        return new AlexTokensSectionImpl(node);
      }
      else if (type == ALEX_TOKEN_SET_DECLARATION) {
        return new AlexTokenSetDeclarationImpl(node);
      }
      else if (type == ALEX_TOP_MODULE_SECTION) {
        return new AlexTopModuleSectionImpl(node);
      }
      else if (type == ALEX_USER_CODE_SECTION) {
        return new AlexUserCodeSectionImpl(node);
      }
      else if (type == ALEX_WRAPPER_TYPE) {
        return new AlexWrapperTypeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
