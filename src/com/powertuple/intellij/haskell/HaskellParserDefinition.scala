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

package com.powertuple.intellij.haskell

import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.{ASTNode, Language, ParserDefinition, PsiParser}
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.{IFileElementType, TokenSet}
import com.intellij.psi.{FileViewProvider, PsiElement, PsiFile, TokenType}
import com.powertuple.intellij.haskell.parser.HaskellParser
import com.powertuple.intellij.haskell.psi.HaskellTypes
import com.powertuple.intellij.haskell.psi.HaskellTypes._
import org.jetbrains.annotations.NotNull

object HaskellParserDefinition {
  final val WHITE_SPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
  final val COMMENTS: TokenSet = TokenSet.create(HaskellTypes.HS_COMMENT, HaskellTypes.HS_NCOMMENT)
  final val RESERVED_IDS: TokenSet = TokenSet.create(HS_CASE, HS_CLASS, HS_DATA, HS_DEFAULT, HS_DERIVING, HS_DO, HS_ELSE, HS_FORALL, HS_IF, HS_IMPORT, HS_IN, HS_INFIX, HS_INFIXL, HS_INFIXR, HS_INSTANCE, HS_LET, HS_MODULE, HS_NEWTYPE, HS_OF, HS_THEN, HS_TYPE, HS_UNSAFE, HS_WHERE, HS_UNDERSCORE)
  final val SPECIAL_RESERVED_IDS = TokenSet.create(HS_TYPE_FAMILY, HS_FOREIGN_IMPORT, HS_FOREIGN_EXPORT, HS_TYPE_INSTANCE)
  final val ALL_RESERVED_IDS = TokenSet.orSet(RESERVED_IDS, SPECIAL_RESERVED_IDS)
  final val RESERVED_OPERATORS: TokenSet = TokenSet.create(HS_DOT_DOT, HS_COLON, HS_COLON_COLON, HS_EQUAL, HS_BACKSLASH, HS_VERTICAL_BAR, HS_LEFT_ARROW, HS_RIGHT_ARROW, HS_AT, HS_TILDE, HS_DOUBLE_RIGHT_ARROW)
  final val OPERATORS: TokenSet = TokenSet.orSet(RESERVED_OPERATORS, TokenSet.create(HS_QVAROP,  HS_QCONOP))
  final val NUMBERS = TokenSet.create(HS_DECIMAL, HS_FLOAT, HS_HEXADECIMAL, HS_OCTAL)
  final val SYMBOLS = TokenSet.create(HS_EXCLAMATION_MARK, HS_HASH, HS_DOLLAR, HS_PERCENTAGE, HS_AMPERSAND, HS_STAR, HS_PLUS, HS_DOT, HS_SLASH, HS_LT, HS_EQUAL, HS_GT, HS_QUESTION_MARK,
    HS_AT, HS_BACKSLASH, HS_CARET, HS_VERTICAL_BAR, HS_TILDE, HS_DASH)
}

class HaskellParserDefinition extends ParserDefinition {

  @NotNull
  def createLexer(project: Project): Lexer = {
    new HaskellLexerAdapter
  }

  def createParser(project: Project): PsiParser = {
    new HaskellParser
  }

  def getFileNodeType: IFileElementType = {
    new IFileElementType(Language.findInstance(classOf[HaskellLanguage]))
  }

  @NotNull
  def getWhitespaceTokens: TokenSet = {
    HaskellParserDefinition.WHITE_SPACES
  }

  @NotNull
  def getCommentTokens: TokenSet = {
    HaskellParserDefinition.COMMENTS
  }

  @NotNull
  def getStringLiteralElements: TokenSet = {
    TokenSet.create(HaskellTypes.HS_CHARACTER_LITERAL, HaskellTypes.HS_STRING_LITERAL)
  }

  @NotNull
  def createElement(node: ASTNode): PsiElement = {
    HaskellTypes.Factory.createElement(node)
  }

  @NotNull
  def createFile(viewProvider: FileViewProvider): PsiFile = {
    new HaskellFile(viewProvider)
  }

  def spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements = {
    SpaceRequirements.MAY
  }
}