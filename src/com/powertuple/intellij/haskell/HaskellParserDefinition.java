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
import com.powertuple.intellij.haskell.psi.HaskellFile;
import com.powertuple.intellij.haskell.psi.HaskellTypes;
import org.jetbrains.annotations.NotNull;

public class HaskellParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(HaskellTypes.HS_COMMENT, HaskellTypes.HS_NCOMMENT);

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
