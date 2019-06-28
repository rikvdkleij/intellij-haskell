package intellij.haskell.cabal.highlighting

import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.editor.colors.{EditorColorsManager, TextAttributesKey}
import com.intellij.psi.PsiElement
import intellij.haskell.cabal.lang.psi.CabalTypes._
import intellij.haskell.cabal.lang.psi._

class CabalAnnotator extends Annotator {

  def annotate(el: PsiElement, h: AnnotationHolder): Unit = {
    el.getNode.getElementType match {
      case _: CabalFieldKeyTokenType => setHighlighting(el, h, CabalSyntaxHighlighter.KEY)
      case _: CabalStanzaKeyTokenType => setHighlighting(el, h, CabalSyntaxHighlighter.CONFIG)
      case _: CabalStanzaArgTokenType => setHighlighting(el, h, CabalSyntaxHighlighter.CONFIG)
      case LBRACE | RBRACE => setHighlighting(el, h, CabalSyntaxHighlighter.COLON)
      case _ => // noop
    }
  }

  private def setHighlighting(element: PsiElement, holder: AnnotationHolder, key: TextAttributesKey): Unit = {
    holder.createInfoAnnotation(element, null).setEnforcedTextAttributes(
      EditorColorsManager.getInstance.getGlobalScheme.getAttributes(key)
    )
  }
}
