package intellij.haskell.alex.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType
import intellij.haskell.alex.lang.lexer.AlexLexer
import intellij.haskell.alex.lang.psi.AlexTypes

object AlexSyntaxHighlighter {
  final val STRINGS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_STRINGS", DefaultLanguageHighlighterColors.STRING)
  final val RULES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_RULES", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val TOKEN_SETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_TOKEN_SETS", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
  final val BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("ALEX_BRACES", DefaultLanguageHighlighterColors.BRACES)
}

class AlexSyntaxHighlighter extends SyntaxHighlighter {
  import com.intellij.openapi.fileTypes.SyntaxHighlighterBase._

  override def getHighlightingLexer: Lexer = new AlexLexer

  override def getTokenHighlights(t: IElementType): Array[TextAttributesKey] = {
    t match {
      case AlexTypes.ALEX_TOKENS => pack(AlexSyntaxHighlighter.KEYWORD)
      case AlexTypes.ALEX_A_SYMBOL_FOLLOWED_BY_TOKENS => pack(AlexSyntaxHighlighter.KEYWORD)
      case AlexTypes.ALEX_STRING => pack(AlexSyntaxHighlighter.STRINGS)
      case AlexTypes.ALEX_DOLLAR_AND_IDENTIFIER => pack(AlexSyntaxHighlighter.TOKEN_SETS)
      case AlexTypes.ALEX_EMAIL_AND_IDENTIFIER => pack(AlexSyntaxHighlighter.RULES)
      case _ => Array()
    }
  }
}
