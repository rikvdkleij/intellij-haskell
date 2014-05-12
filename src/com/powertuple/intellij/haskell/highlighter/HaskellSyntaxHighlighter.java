package com.powertuple.intellij.haskell.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.powertuple.intellij.haskell.HaskellLexer;
import com.powertuple.intellij.haskell.psi.HaskellTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HaskellSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

    static final TokenSet LINE_COMMENTS = TokenSet.create(
            HaskellTypes.HS_COMMENT
    );

    static final TokenSet NESTED_COMMENTS = TokenSet.create(
            HaskellTypes.HS_NCOMMENT
    );

    static final TokenSet KEYWORDS = TokenSet.create(
            HaskellTypes.HS_CASE_KEYWORD,
            HaskellTypes.HS_MODULE_KEYWORD,
            HaskellTypes.HS_WHERE_KEYWORD
    );

    static final TokenSet OPERATORS = TokenSet.create(
            HaskellTypes.HS_DEFINED_BY,
            HaskellTypes.HS_DRAW_FROM_OR_MATCHES_OR_IN
    );

    static {
        SyntaxHighlighterBase.fillMap(ATTRIBUTES, KEYWORDS, DefaultLanguageHighlighterColors.KEYWORD);
        SyntaxHighlighterBase.fillMap(ATTRIBUTES, LINE_COMMENTS, DefaultLanguageHighlighterColors.LINE_COMMENT);
        SyntaxHighlighterBase.fillMap(ATTRIBUTES, NESTED_COMMENTS, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
        SyntaxHighlighterBase.fillMap(ATTRIBUTES, OPERATORS, DefaultLanguageHighlighterColors.OPERATION_SIGN);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new HaskellLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}