package intellij.haskell.cabal.lang.parser;

import intellij.haskell.cabal.CabalLanguage;
import intellij.haskell.cabal.lang.lexer.CabalParsingLexer;
import intellij.haskell.cabal.lang.psi.CabalElementFactory;
import intellij.haskell.cabal.lang.psi.CabalFile;
import intellij.haskell.cabal.lang.psi.CabalTypes;
import com.intellij.lang.ASTNode;
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
import org.jetbrains.annotations.NotNull;

public class CabalParserDefinition implements ParserDefinition {

  // NOTE: You must use CabalLanguage.INSTANCE instead of Language.findInstance()
  // since the language may not have been initialized yet.
  public static final IFileElementType FILE = new IFileElementType(CabalLanguage.Instance);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new CabalParsingLexer();
  }

  @Override
  public PsiParser createParser(Project project) {
    return new CabalParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return TokenSet.create(TokenType.WHITE_SPACE);
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return TokenSet.create(CabalTypes.COMMENT);
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return CabalElementFactory.createElement(node);
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new CabalFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
