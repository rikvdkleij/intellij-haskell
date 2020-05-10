package intellij.haskell.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.{FoldingBuilderEx, FoldingDescriptor}
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil

class HaskellFoldingBuilder extends FoldingBuilderEx with DumbAware {

  override def buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array[FoldingDescriptor] = {
    root match {
      case file: HaskellFile => HaskellPsiUtil.findImportDeclarationsBlock(file).map(d => Array(new FoldingDescriptor(d, new TextRange(d.getTextRange.getStartOffset, d.getTextRange.getEndOffset - 1)))).getOrElse(FoldingDescriptor.EMPTY)
      case _ => FoldingDescriptor.EMPTY
    }
  }

  override def isCollapsedByDefault(node: ASTNode): Boolean = {
    true
  }

  override def getPlaceholderText(node: ASTNode): String = {
    "import ..."
  }
}
