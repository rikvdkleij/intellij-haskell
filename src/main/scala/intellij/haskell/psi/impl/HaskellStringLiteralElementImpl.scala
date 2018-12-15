package intellij.haskell.psi.impl

import java.lang

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.{LiteralTextEscaper, PsiFileFactory, PsiLanguageInjectionHost}
import intellij.haskell.alex.AlexLanguage
import intellij.haskell.psi.HaskellStringLiteralElement

abstract class HaskellStringLiteralElementImpl private[impl](node: ASTNode)
  extends HaskellCompositeElementImpl(node)
    with HaskellStringLiteralElement
    with PsiLanguageInjectionHost {
  override def isValidHost: Boolean = {
    true
  }

  override def updateText(text: String): HaskellStringLiteralElementImpl = {
    val newElement = PsiFileFactory
      .getInstance(getProject)
      .createFileFromText("a.hs", AlexLanguage.Instance, text, false, false)
    this.replace(newElement).asInstanceOf[HaskellStringLiteralElementImpl]
  }

  override def createLiteralTextEscaper(): LiteralTextEscaper[HaskellStringLiteralElementImpl] = {
    LiteralTextEscaper.createSimple(this)
  }
}
