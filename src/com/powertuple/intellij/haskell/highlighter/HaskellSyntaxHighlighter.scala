/*
 * Copyright 2015 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powertuple.intellij.haskell.highlighter

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.colors.{CodeInsightColors, TextAttributesKey}
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.powertuple.intellij.haskell.HaskellLexer
import com.powertuple.intellij.haskell.psi.HaskellTypes._
import org.jetbrains.annotations.NotNull

object HaskellSyntaxHighlighter {
  final val Illegal = createTextAttributesKey("HS_ILLEGAL", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
  final val Comment = createTextAttributesKey("HS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
  final val BlockComment = createTextAttributesKey("HS_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
  final val String = createTextAttributesKey("HS_STRING", DefaultLanguageHighlighterColors.STRING)
  final val Number = createTextAttributesKey("HS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
  final val Keyword = createTextAttributesKey("HS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
  final val Parentheses = createTextAttributesKey("HS_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
  final val Brace = createTextAttributesKey("HSL_BRACE", DefaultLanguageHighlighterColors.BRACES)
  final val Bracket = createTextAttributesKey("HS_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
  final val Variable = createTextAttributesKey("HS_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
  final val Constructor = createTextAttributesKey("HS_CONSTRUCTOR", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  final val Operator = createTextAttributesKey("HS_OPERATOR", CodeInsightColors.TYPE_PARAMETER_NAME_ATTRIBUTES)
  final val ReservedSymbol = createTextAttributesKey("HS_SYMBOL", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
  final val Pragma = createTextAttributesKey("HS_PRAGMA", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
  final val Default = createTextAttributesKey("HS_DEFAULT", DefaultLanguageHighlighterColors.COMMA)
}

class HaskellSyntaxHighlighter extends SyntaxHighlighterBase {

  import com.intellij.openapi.fileTypes.SyntaxHighlighterBase._
  import com.powertuple.intellij.haskell.HaskellParserDefinition._

  @NotNull
  def getHighlightingLexer: Lexer = {
    new HaskellLexer
  }

  @NotNull
  def getTokenHighlights(elementType: IElementType): Array[TextAttributesKey] = {
    import com.powertuple.intellij.haskell.highlighter.HaskellSyntaxHighlighter._

    elementType match {
      case TokenType.BAD_CHARACTER => pack(Illegal)
      case et if PRAGMA_START_END_IDS.contains(et) => pack(Pragma)
      case et if et == HS_COMMENT => pack(Comment)
      case et if et == HS_NCOMMENT => pack(BlockComment)
      case et if et == HS_STRING_LITERAL || et == HS_CHARACTER_LITERAL => pack(String)
      case et if NUMBERS.contains(et) => pack(Number)
      case et if et == HS_LEFT_PAREN || et == HS_RIGHT_PAREN => pack(Parentheses)
      case et if et == HS_LEFT_BRACE || et == HS_RIGHT_BRACE => pack(Brace)
      case et if et == HS_LEFT_BRACKET || et == HS_RIGHT_BRACKET => pack(Bracket)
      case et if ALL_RESERVED_IDS.contains(et) => pack(Keyword)
      case et if SYMBOLS_RES_OP.contains(et) => pack(ReservedSymbol)
      case et if OPERATORS.contains(et) => pack(Operator)
      case et if et == HS_VARID_ID => pack(Variable)
      case et if et == HS_CONID_ID => pack(Constructor)
      case et if WHITE_SPACES.contains(et) || et == HS_NEWLINE => pack(null)
      case _ => pack(Default)
    }
  }
}