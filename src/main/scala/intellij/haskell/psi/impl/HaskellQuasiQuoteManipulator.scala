package intellij.haskell.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import intellij.haskell.psi.HaskellElementFactory
import org.jetbrains.annotations.Nullable

/**
  * @author ice1000
  */
class HaskellQuasiQuoteManipulator extends AbstractElementManipulator[HaskellQuasiQuoteElementImpl] {
  @Nullable
  @throws[IncorrectOperationException]
  override def handleContentChange(psi: HaskellQuasiQuoteElementImpl,
                                   range: TextRange,
                                   newContent: String): HaskellQuasiQuoteElementImpl = {
    val oldText = psi.getText
    val newText = oldText.substring(0, range.getStartOffset) + newContent + oldText.substring(range.getEndOffset)
    val newElement = HaskellElementFactory.createQuasiQuote(psi.getProject, newText)
    newElement.map(psi.replace(_).asInstanceOf[HaskellQuasiQuoteElementImpl]).orNull
  }

  override def getRangeInElement(element: HaskellQuasiQuoteElementImpl): TextRange = {
    val text = element.getText
    new TextRange(text.indexOf('|') + 1, text.lastIndexOf('|'))
  }
}

