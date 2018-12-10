package intellij.haskell.alex.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType
import intellij.haskell.alex.lang.lexer.AlexLexer
import intellij.haskell.alex.lang.psi.AlexTypes

/**
  * @author ice1000
  */
object AlexSyntaxHighlighter {
  final val STRINGS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_STRINGS", DefaultLanguageHighlighterColors.STRING)
  final val RULES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_RULES", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val TOKEN_SETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_TOKEN_SETS", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
  final val SEMICOLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_SEMI", DefaultLanguageHighlighterColors.SEMICOLON)
  final val BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_BRACES", DefaultLanguageHighlighterColors.BRACES)
  final val PARENTHESIS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_PAREN", DefaultLanguageHighlighterColors.PARENTHESES)
}

/**
  * @author ice1000
  */
class AlexSyntaxHighlighter extends SyntaxHighlighter {
  import com.intellij.openapi.fileTypes.SyntaxHighlighterBase._

  override def getHighlightingLexer: Lexer = new AlexLexer

  override def getTokenHighlights(t: IElementType): Array[TextAttributesKey] = {
    t match {
      case AlexTypes.ALEX_A_SYMBOL_FOLLOWED_BY_TOKENS => pack(AlexSyntaxHighlighter.KEYWORD)
      case AlexTypes.ALEX_STRING => pack(AlexSyntaxHighlighter.STRINGS)
      case AlexTypes.ALEX_PUBLIC_REGEX => pack(AlexSyntaxHighlighter.STRINGS)
      case AlexTypes.ALEX_HASKELL_IDENTIFIER => pack(AlexSyntaxHighlighter.STRINGS)
      case AlexTypes.ALEX_SOMETHING_IS_GONNA_HAPPEN => pack(AlexSyntaxHighlighter.BRACES)
      case AlexTypes.ALEX_SOMETHING_HAS_ALREADY_HAPPENED => pack(AlexSyntaxHighlighter.BRACES)
      case AlexTypes.ALEX_LEFT_LISP => pack(AlexSyntaxHighlighter.PARENTHESIS)
      case AlexTypes.ALEX_RIGHT_LISP => pack(AlexSyntaxHighlighter.PARENTHESIS)
      case AlexTypes.ALEX_SEMICOLON => pack(AlexSyntaxHighlighter.SEMICOLON)
      case AlexTypes.ALEX_DOLLAR_AND_IDENTIFIER => pack(AlexSyntaxHighlighter.TOKEN_SETS)
      case AlexTypes.ALEX_EMAIL_AND_IDENTIFIER => pack(AlexSyntaxHighlighter.RULES)
      case _ => Array()
    }
  }
}
