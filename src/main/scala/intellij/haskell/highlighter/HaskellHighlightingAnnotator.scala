package intellij.haskell.highlighter

import com.intellij.lang.annotation.{AnnotationHolder, Annotator, HighlightSeverity}
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import intellij.haskell.psi._
import intellij.haskell.psi.impl.HaskellStringLiteralElementImpl

class HaskellHighlightingAnnotator extends Annotator {
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    element match {
      case psi: HaskellImportQualified => HighlightingAnnotator.infoAnnotation(holder, psi, HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportQualifiedAs => HighlightingAnnotator.infoAnnotation(holder, psi.getFirstChild, HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportHiding => HighlightingAnnotator.infoAnnotation(holder, psi, HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellPragma => HighlightingAnnotator.infoAnnotation(holder, psi, HaskellSyntaxHighlighter.PragmaContent)
        Option(psi.getFirstChild).foreach(c => HighlightingAnnotator.infoAnnotation(holder, c, HaskellSyntaxHighlighter.Pragma))
        Option(psi.getLastChild).map(_.getPrevSibling).foreach(c => HighlightingAnnotator.infoAnnotation(holder, c, HaskellSyntaxHighlighter.Pragma))
      case psi: HaskellTypeSignature => HighlightingAnnotator.infoAnnotation(holder, psi.getFirstChild, HaskellSyntaxHighlighter.FunctionName)
      case psi: HaskellStringLiteralElementImpl => HighlightingAnnotator.infoAnnotation(holder, psi, HaskellSyntaxHighlighter.String)
      case _ =>
    }
  }
}

object HighlightingAnnotator {
  def infoAnnotation(holder: AnnotationHolder, psi: PsiElement, attribute: TextAttributesKey): Unit = {
    holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "")
      .range(psi)
      .textAttributes(attribute)
      .create()
  }
}