package intellij.haskell.alex.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import intellij.haskell.alex.lang.psi.AlexElement

/**
  * @author ice1000
  */
class AlexElementImpl(node: ASTNode) extends ASTWrapperPsiElement(node) with AlexElement {
}
