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
      .createFileFromText("a.x", AlexLanguage.Instance, text, false, false)
    this.replace(newElement).asInstanceOf[HaskellStringLiteralElementImpl]
  }

  override def createLiteralTextEscaper(): LiteralTextEscaper[HaskellStringLiteralElementImpl] = {
    new LiteralTextEscaper[HaskellStringLiteralElementImpl](this) {
      override def decode(textRange: TextRange, stringBuilder: lang.StringBuilder): Boolean = {
        stringBuilder.append(myHost.getText, textRange.getStartOffset, textRange.getEndOffset)
        true
      }

      override def getOffsetInHost(i: Int, textrange: TextRange): Int = {
        // TODO: deal with escaping
        var j = i + textrange.getStartOffset
        if (j < textrange.getStartOffset) j = textrange.getStartOffset
        if (j > textrange.getEndOffset) j = textrange.getEndOffset
        j
      }

      override def isOneLine: Boolean = {
        false
      }
    }
  }
}
