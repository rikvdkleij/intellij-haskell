/*
 * Copyright 2014 Rik van der Kleij
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
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
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
  final val Operator = createTextAttributesKey("HS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
  final val Parentheses = createTextAttributesKey("HS_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
  final val Brace = createTextAttributesKey("HSL_BRACE", DefaultLanguageHighlighterColors.BRACES)
  final val Bracket = createTextAttributesKey("HS_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
  final val Variable = createTextAttributesKey("HS_VAR", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
  final val Constructor = createTextAttributesKey("HS_CONSTRUCTOR", DefaultLanguageHighlighterColors.CLASS_NAME)
  final val Default = createTextAttributesKey("HS_DEFAULT", DefaultLanguageHighlighterColors.FUNCTION_CALL)
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
      case et if et == HS_COMMENT => pack(Comment)
      case et if et == HS_NCOMMENT => pack(BlockComment)
      case et if et == HS_STRING_LITERAL || et == HS_CHARACTER_LITERAL => pack(String)
      case et if NUMBERS.contains(et) => pack(Number)
      case et if RESERVED_IDS.contains(et) => pack(Keyword)
      case et if OPERATORS.contains(et) => pack(Operator)
      case et if et == HS_LEFT_PAREN || et == HS_RIGHT_PAREN => pack(Parentheses)
      case et if et == HS_LEFT_BRACE || et == HS_RIGHT_BRACE => pack(Brace)
      case et if et == HS_LEFT_BRACKET || et == HS_RIGHT_BRACKET => pack(Bracket)
      case et if et == HS_QVAR => pack(Variable)
      //      case et if et == HS_VAR_ID || SYMBOLS.contains(et) => pack(Variable)
      case et if et == HS_QCON => pack(Constructor)
      case _ => pack(Default)
    }
  }
}