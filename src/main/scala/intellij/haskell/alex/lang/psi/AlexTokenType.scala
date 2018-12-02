package intellij.haskell.alex.lang.psi

import com.intellij.psi.tree.IElementType
import intellij.haskell.alex.AlexLanguage

/**
  * @author ice1000
  */
class AlexTokenType(debugName: String) extends IElementType(debugName, AlexLanguage.Instance) {
}
