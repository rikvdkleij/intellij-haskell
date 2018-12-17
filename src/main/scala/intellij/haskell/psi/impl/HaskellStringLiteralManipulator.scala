package intellij.haskell.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.{AbstractElementManipulator, PsiFileFactory}
import com.intellij.util.IncorrectOperationException
import intellij.haskell.HaskellLanguage
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
    val newElement = PsiFileFactory
      .getInstance(psi.getProject)
      .createFileFromText("a.hs", HaskellLanguage.Instance, newContent, false, false)
    psi.replace(newElement).asInstanceOf[HaskellStringLiteralElementImpl]
  }

  override def getRangeInElement(element: HaskellStringLiteralElementImpl): TextRange = {
    new TextRange(1, element.getTextLength - 1)
  }
}

