/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.powertuple.intellij.haskell.parser.HaskellParser;
import com.powertuple.intellij.haskell.psi.HaskellTypes;
import org.jetbrains.annotations.NotNull;

import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;

public class HaskellParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(HaskellTypes.HS_COMMENT, HaskellTypes.HS_NCOMMENT);

    public static final TokenSet RESERVED_IDS = TokenSet.create(
            HS_CASE, HS_CLASS, HS_DATA, HS_DEFAULT, HS_DERIVING, HS_DO, HS_ELSE, HS_FOREIGN,
            HS_IF, HS_IMPORT, HS_IN, HS_INFIX, HS_INFIXL, HS_INFIXR, HS_INSTANCE, HS_LET,
            HS_MODULE, HS_NEWTYPE, HS_OF, HS_THEN, HS_TYPE, HS_WHERE, HS_UNDERSCORE,
            HS_AS, HS_QUALIFIED, HS_HIDING, HS_INCLUDE
    );

    public static final TokenSet OPERATORS = TokenSet.create(
            HS_DOT_DOT, HS_COLON, HS_COLON_COLON, HS_EQUAL, HS_SLASH, HS_VERTICAL_BAR,
            HS_LEFT_ARROW, HS_RIGHT_ARROW, HS_AT, HS_TILDE, HS_DOUBLE_RIGHT_ARROW,
            HS_QVARSYM, HS_VARSYM, HS_QCONSYM, HS_CONSYM
    );

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new HaskellLexerAdapter();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new HaskellParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return new IFileElementType(Language.findInstance(HaskellLanguage.class));
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return TokenSet.create(
                HaskellTypes.HS_CHARACTER_LITERAL,
                HaskellTypes.HS_STRING_LITERAL
        );
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return HaskellTypes.Factory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new HaskellFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}