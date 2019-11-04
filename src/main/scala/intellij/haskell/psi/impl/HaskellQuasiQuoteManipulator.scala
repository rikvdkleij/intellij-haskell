package intellij.haskell.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.{AbstractElementManipulator, PsiFileFactory}
import com.intellij.util.IncorrectOperationException
import intellij.haskell.HaskellLanguage
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
    val newElement = PsiFileFactory
      .getInstance(psi.getProject)
      .createFileFromText("a.hs", HaskellLanguage.Instance, newText, false, false)
      .getLastChild
      .getLastChild
    psi.replace(newElement).asInstanceOf[HaskellQuasiQuoteElementImpl]
  }

  override def getRangeInElement(element: HaskellQuasiQuoteElementImpl): TextRange = {
    val text = element.getText
    new TextRange(text.indexOf('|') + 1, text.lastIndexOf('|'))
  }
}

