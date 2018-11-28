package intellij.haskell.highlighter

import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement
import intellij.haskell.psi._

class HaskellSoftKeywordsAnnotator extends Annotator {
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    element match {
      case psi: HaskellImportQualified => holder.createInfoAnnotation(psi, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportQualifiedAs => holder.createInfoAnnotation(psi.getFirstChild, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportHiding => holder.createInfoAnnotation(psi, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellFileHeaderPragma => psi.getGeneralPragmaContentList.forEach((t: HaskellGeneralPragmaContent) =>
        holder.createInfoAnnotation(t, null).setTextAttributes(HaskellSyntaxHighlighter.PragmaContent))
      case psi: HaskellTypeSignature => holder.createInfoAnnotation(psi.getFirstChild, null)
          .setTextAttributes(HaskellSyntaxHighlighter.FunctionName)
      case _ =>
    }
  }
}
