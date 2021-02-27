/*
 * Copyright 2014-2020 Rik van der Kleij
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

package intellij.haskell

import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.{ASTNode, ParserDefinition, PsiParser}
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi._
import com.intellij.psi.tree.{IFileElementType, TokenSet}
import intellij.haskell.HaskellParserDefinition.{Comments, WhiteSpaces}
import intellij.haskell.parser.HaskellParser
import intellij.haskell.psi.HaskellTypes
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi.stubs.types.HaskellFileElementType
import org.jetbrains.annotations.NotNull

//noinspection TypeAnnotation
object HaskellParserDefinition {
  final val WhiteSpaces = TokenSet.create(TokenType.WHITE_SPACE, HS_NEWLINE)
  final val Comments = TokenSet.create(HS_COMMENT, HS_NCOMMENT, HS_HADDOCK, HS_NHADDOCK)
  final val PragmaStartEndIds = TokenSet.create(HS_PRAGMA_START, HS_PRAGMA_END)
  final val ReservedIdS = TokenSet.create(HS_CASE, HS_CLASS, HS_DATA, HS_DEFAULT, HS_DERIVING, HS_DO, HS_ELSE, HS_IF, HS_IMPORT,
    HS_IN, HS_INFIX, HS_INFIXL, HS_INFIXR, HS_INSTANCE, HS_LET, HS_MODULE, HS_NEWTYPE, HS_OF, HS_THEN, HS_TYPE, HS_WHERE, HS_UNDERSCORE)
  final val SpecialReservedIds = TokenSet.create(HS_TYPE_FAMILY, HS_FOREIGN_IMPORT, HS_FOREIGN_EXPORT, HS_TYPE_INSTANCE)
  final val AllReservedIds = TokenSet.orSet(ReservedIdS, SpecialReservedIds)
  final val ReservedOperators = TokenSet.create(HS_COLON_COLON, HS_EQUAL, HS_BACKSLASH, HS_VERTICAL_BAR, HS_LEFT_ARROW,
    HS_RIGHT_ARROW, HS_AT, HS_TILDE, HS_DOUBLE_RIGHT_ARROW, HS_DOT_DOT)
  final val Operators = TokenSet.orSet(ReservedOperators, TokenSet.create(HS_VARSYM_ID, HS_CONSYM_ID), TokenSet.create(HS_DOT))
  final val NumberLiterals = TokenSet.create(HS_DECIMAL, HS_FLOAT, HS_HEXADECIMAL, HS_OCTAL)
  final val SymbolsResOp = TokenSet.create(HS_EQUAL, HS_AT, HS_BACKSLASH, HS_VERTICAL_BAR, HS_TILDE)
  final val StringLiterals = TokenSet.create(HS_CHARACTER_LITERAL, HS_STRING_LITERAL)
  final val Literals = TokenSet.orSet(StringLiterals, NumberLiterals, TokenSet.create(HS_QUASIQUOTE))
  final val HaskellParser = new HaskellParser
  final val Ids = TokenSet.create(HS_VAR_ID, HS_CON_ID, HS_VARSYM_ID, HS_CONSYM_ID)
}

class HaskellParserDefinition extends ParserDefinition {

  @NotNull
  def createLexer(project: Project): Lexer = {
    new HaskellLayoutLexer(
      new HaskellLexerAdapter,
      HaskellTypes.HS_NEWLINE,
      HaskellTypes.HS_LEFT_BRACE,
      HaskellTypes.HS_SEMICOLON,
      HaskellTypes.HS_RIGHT_BRACE,
      TokenSet.orSet(Comments, WhiteSpaces, TokenSet.create(HS_LEFT_BRACE, HS_SEMICOLON, HS_RIGHT_BRACE)),
      TokenSet.create(HS_WHERE, HS_LET, HS_DO, HS_OF),
      LetIn(HS_LET, HS_IN)
    )
  }

  def createParser(project: Project): PsiParser = {
    HaskellParserDefinition.HaskellParser
  }

  def getFileNodeType: IFileElementType = {
    HaskellFileElementType.Instance
  }

  @NotNull
  override def getWhitespaceTokens: TokenSet = {
    HaskellParserDefinition.WhiteSpaces
  }

  @NotNull
  def getCommentTokens: TokenSet = {
    HaskellParserDefinition.Comments
  }

  @NotNull
  def getStringLiteralElements: TokenSet = {
    HaskellParserDefinition.StringLiterals
  }

  @NotNull
  def createElement(node: ASTNode): PsiElement = {
    Factory.createElement(node)
  }

  @NotNull
  def createFile(viewProvider: FileViewProvider): PsiFile = {
    new HaskellFile(viewProvider)
  }

  override def spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements = {
    SpaceRequirements.MAY
  }
}