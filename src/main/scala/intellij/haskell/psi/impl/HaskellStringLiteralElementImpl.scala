package intellij.haskell.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.{ElementManipulators, LiteralTextEscaper, PsiLanguageInjectionHost}
import intellij.haskell.psi.HaskellStringLiteralElement

abstract class HaskellStringLiteralElementImpl private[impl](node: ASTNode)
  extends HaskellCompositeElementImpl(node)
    with HaskellStringLiteralElement
    with PsiLanguageInjectionHost {
  override def isValidHost: Boolean = {
    true
  }

  override def updateText(text: String): HaskellStringLiteralElementImpl = {
    ElementManipulators.handleContentChange(this, text)
  }

  override def createLiteralTextEscaper(): LiteralTextEscaper[HaskellStringLiteralElementImpl] = {
    new HaskellStringEscaper(this)
  }
}
