package intellij.haskell.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.{ElementManipulators, LiteralTextEscaper, PsiLanguageInjectionHost}
import intellij.haskell.psi.HaskellQuasiQuoteElement

abstract class HaskellQuasiQuoteElementImpl private[impl](node: ASTNode)
  extends HaskellCompositeElementImpl(node)
    with HaskellQuasiQuoteElement
    with PsiLanguageInjectionHost {
  override def isValidHost: Boolean = {
    true
  }

  override def updateText(text: String): HaskellQuasiQuoteElementImpl = {
    ElementManipulators.handleContentChange(this, text)
  }

  override def createLiteralTextEscaper(): LiteralTextEscaper[HaskellQuasiQuoteElementImpl] = {
    LiteralTextEscaper.createSimple(this)
  }
}
