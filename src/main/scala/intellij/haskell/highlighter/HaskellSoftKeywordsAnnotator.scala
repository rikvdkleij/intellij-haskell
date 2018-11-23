package intellij.haskell.highlighter

import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement
import intellij.haskell.psi.{HaskellImportHiding, HaskellImportQualified, HaskellImportQualifiedAs}

class HaskellSoftKeywordsAnnotator extends Annotator {
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    element match {
      case _: HaskellImportQualified => holder.createInfoAnnotation(element, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case _: HaskellImportQualifiedAs => holder.createInfoAnnotation(element.getFirstChild, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case _: HaskellImportHiding => holder.createInfoAnnotation(element, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case _ =>
    }
  }
}
