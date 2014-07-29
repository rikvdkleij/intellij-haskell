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

package com.powertuple.intellij.haskell.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.powertuple.intellij.haskell.HaskellLexer;
import com.powertuple.intellij.haskell.HaskellParserDefinition;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;
import static com.powertuple.intellij.haskell.HaskellParserDefinition.OPERATORS;
import static com.powertuple.intellij.haskell.HaskellParserDefinition.RESERVED_IDS;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;

public class HaskellSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey ILLEGAL = createTextAttributesKey("HS_ILLEGAL", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("HS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey STRING = createTextAttributesKey("HS_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("HS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("HS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OPERATOR = createTextAttributesKey("HS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PARENTHESES = createTextAttributesKey("HS_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACE = createTextAttributesKey("HSL_BRACE", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKET = createTextAttributesKey("HS_BRACKET", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey SYMBOL = createTextAttributesKey("HS_SYMBOL", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey CONSTRUCTOR = createTextAttributesKey("HS_CONSTRUCTOR", CodeInsightColors.CONSTRUCTOR_CALL_ATTRIBUTES);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new HaskellLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (type == TokenType.BAD_CHARACTER) {
            return pack(ILLEGAL);
        }
        if (HaskellParserDefinition.COMMENTS.contains(type)) {
            return pack(COMMENT);
        }
        if (type == HS_STRING_LITERAL || type == HS_CHARACTER_LITERAL) {
            return pack(STRING);
        }
        if (type == HS_DECIMAL || type == HS_FLOAT | type == HS_HEXADECIMAL | type == HS_OCTAL) {
            return pack(NUMBER);
        }
        if (RESERVED_IDS.contains(type)) {
            return pack(KEYWORD);
        }
        if (OPERATORS.contains(type)) {
            return pack(OPERATOR);
        }
        if (type == HS_LEFT_PAREN || type == HS_RIGHT_PAREN) {
            return pack(PARENTHESES);
        }
        if (type == HS_LEFT_BRACE || type == HS_RIGHT_BRACE) {
            return pack(BRACE);
        }
        if (type == HS_LEFT_BRACKET || type == HS_RIGHT_BRACKET) {
            return pack(BRACKET);
        }
        if (type == HS_VAR) {
            return pack(SYMBOL);
        }
        if (type == HS_CON) {
            return pack(CONSTRUCTOR);
        }
        return EMPTY;
    }
}
