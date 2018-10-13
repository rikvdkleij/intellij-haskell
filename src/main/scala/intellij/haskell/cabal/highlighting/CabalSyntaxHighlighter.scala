package intellij.haskell.cabal.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import intellij.haskell.cabal.lang.psi.CabalTypes

object CabalSyntaxHighlighter {
  final val COLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN)
  final val KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
  final val CONFIG: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_CONFIG", DefaultLanguageHighlighterColors.NUMBER)
  final val CONDITIONAL: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_CONDITIONAL", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
  final val BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("CABAL_BRACES", DefaultLanguageHighlighterColors.BRACES)
}

class CabalSyntaxHighlighter extends SyntaxHighlighterBase {

  import com.intellij.openapi.fileTypes.SyntaxHighlighterBase._

  def getHighlightingLexer = new CabalSyntaxHighlightingLexer

  def getTokenHighlights(elementType: IElementType): Array[TextAttributesKey] = {
    elementType match {
      case CabalTypes.COLON => pack(CabalSyntaxHighlighter.COLON)
      case CabalTypes.COMMENT => pack(CabalSyntaxHighlighter.COMMENT)
      case CabalTypes.KEY => pack(CabalSyntaxHighlighter.KEY)
      case CabalTypes.CONFIG => pack(CabalSyntaxHighlighter.CONFIG)
      case CabalTypes.CONDITIONAL => pack(CabalSyntaxHighlighter.CONDITIONAL)
      case CabalTypes.LBRACE | CabalTypes.RBRACE => pack(CabalSyntaxHighlighter.BRACES)
      case _ => new Array[TextAttributesKey](0)
    }
  }
}
