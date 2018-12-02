package intellij.haskell.alex.lang.psi.impl

import java.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.{PsiFileFactory, PsiLanguageInjectionHost}
import intellij.haskell.alex.AlexLanguage

abstract class AlexHaskellCodeInjectionHost(node: ASTNode)
  extends AlexElementImpl(node) with PsiLanguageInjectionHost {

  import com.intellij.openapi.util.TextRange
  import com.intellij.psi.LiteralTextEscaper

  override def isValidHost = true

  override def updateText(text: String): AlexHaskellCodeInjectionHost = {
    val newElement = PsiFileFactory
      .getInstance(getProject)
      .createFileFromText("a.x", AlexLanguage.Instance, text, false, false)
    this.replace(newElement).asInstanceOf[AlexHaskellCodeInjectionHost]
  }

  override def createLiteralTextEscaper: LiteralTextEscaper[AlexHaskellCodeInjectionHost] = {
    new LiteralTextEscaper[AlexHaskellCodeInjectionHost](this) {
      override def decode(textRange: TextRange, stringBuilder: lang.StringBuilder): Boolean = {
        stringBuilder.append(myHost.getText, textRange.getStartOffset, textRange.getEndOffset)
        true
      }

      override def getOffsetInHost(i: Int, textrange: TextRange): Int = {
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
