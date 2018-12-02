package intellij.haskell.alex.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nullable

class AlexElementManipulator extends AbstractElementManipulator[AlexHaskellCodeInjectionHost] {
  @Nullable
  @throws[IncorrectOperationException]
  override def handleContentChange(psi: AlexHaskellCodeInjectionHost,
                                   range: TextRange,
                                   newContent: String): AlexHaskellCodeInjectionHost = {
    val oldText = psi.getText
    val newText = oldText.substring(0, range.getStartOffset) + newContent + oldText.substring(range.getEndOffset)
    psi.updateText(newText)
  }
}

