package intellij.haskell.alex.lang.parser

import com.intellij.lang.{ASTNode, ParserDefinition, PsiParser}
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.{IFileElementType, TokenSet}
import com.intellij.psi.{FileViewProvider, PsiElement, PsiFile}
import intellij.haskell.alex.lang.lexer.AlexLexer
import intellij.haskell.alex.lang.psi.AlexTypes
import intellij.haskell.alex.{AlexFile, AlexLanguage}

/**
  * @author ice1000
  */
object AlexParserDefinition {
  final val FILE = new IFileElementType(AlexLanguage.Instance)

  final val STRINGS = TokenSet.create(AlexTypes.ALEX_STRING)
}

/**
  * @author ice1000
  */
class AlexParserDefinition extends ParserDefinition {
  override def createLexer(project: Project): Lexer = {
    new AlexLexer
  }

  override def createParser(project: Project): PsiParser = {
    new AlexParser
  }

  override def getFileNodeType: IFileElementType = {
    AlexParserDefinition.FILE
  }

  override def getCommentTokens: TokenSet = {
    TokenSet.EMPTY
  }

  override def getStringLiteralElements: TokenSet = {
    AlexParserDefinition.STRINGS
  }

  override def createElement(astNode: ASTNode): PsiElement = {
    AlexTypes.Factory.createElement(astNode)
  }

  override def createFile(fileViewProvider: FileViewProvider): PsiFile = {
    new AlexFile(fileViewProvider)
  }
}
