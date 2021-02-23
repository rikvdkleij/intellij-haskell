package intellij.haskell.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.{FoldingBuilderEx, FoldingDescriptor}
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.HaskellFile
import intellij.haskell.psi._

class HaskellFoldingBuilder extends FoldingBuilderEx with DumbAware {

  override def buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array[FoldingDescriptor] = {
    root match {
      case file: HaskellFile =>
        HaskellPsiUtil.findImportDeclarationsBlock(file).map(createFoldingDescriptor).getOrElse(Array()) ++
          HaskellPsiUtil.findFileHeader(file).map(createFoldingDescriptor).getOrElse(Array()) ++
          HaskellPsiUtil.findTopLevelExpressions(file).flatMap(createFoldingDescriptor)
      case _ => FoldingDescriptor.EMPTY
    }
  }

  private def createFoldingDescriptor(element: PsiElement): Array[FoldingDescriptor] = {
    Array(new FoldingDescriptor(element, createFoldingTextRange(element)))
  }

  private def createFoldingTextRange(element: PsiElement) = {
    if (PsiTreeUtil.lastChild(element).getNode.getElementType == HaskellTypes.HS_NEWLINE) {
      new TextRange(element.getTextRange.getStartOffset, element.getTextRange.getEndOffset - 1)
    } else {
      new TextRange(element.getTextRange.getStartOffset, element.getTextRange.getEndOffset)
    }
  }

  override def isCollapsedByDefault(node: ASTNode): Boolean = {
    val foldingSettings = HaskellFoldingSettings.getInstance()
    if (node.getElementType == HaskellTypes.HS_IMPORT_DECLARATIONS) {
      foldingSettings.isCollapseImports
    } else if (node.getElementType == HaskellTypes.HS_FILE_HEADER) {
      foldingSettings.isCollapseImports
    } else if (node.getElementType == HaskellTypes.HS_EXPRESSION) {
      foldingSettings.isCollapseTopLevelExpression
    } else false
  }

  override def getPlaceholderText(node: ASTNode): String = {
    node.getPsi match {
      case _: HaskellImportDeclarations => "import ..."
      case _: HaskellFileHeader => "{-# ... #-}"
      case e: HaskellExpression => Option(e.getFirstChild.getText).getOrElse("") + " ..."
      case _ => null
    }
  }
}
