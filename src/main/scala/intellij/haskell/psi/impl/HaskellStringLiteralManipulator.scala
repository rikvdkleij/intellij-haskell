package intellij.haskell.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nullable

/**
  * @author ice1000
  */
class HaskellStringLiteralManipulator extends AbstractElementManipulator[HaskellStringLiteralElementImpl] {
  @Nullable
  @throws[IncorrectOperationException]
  override def handleContentChange(psi: HaskellStringLiteralElementImpl,
                                   range: TextRange,
                                   newContent: String): HaskellStringLiteralElementImpl = {
    val oldText = psi.getText
    val newText = oldText.substring(0, range.getStartOffset) + newContent + oldText.substring(range.getEndOffset)
    psi.updateText(newText)
  }
}

