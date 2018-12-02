package intellij.haskell.alex.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.{PsiElement, PsiNameIdentifierOwner, PsiReference}
import com.intellij.util.IncorrectOperationException
import intellij.haskell.alex.lang.psi.{AlexTokenSetId, AlexTokenSetReference}

/**
  * @author ice1000
  * @param node ast node
  */
abstract class AlexTokenSetIdMixin(node: ASTNode) extends AlexElementImpl(node) with AlexTokenSetId with PsiNameIdentifierOwner {
  override def getReference: AlexTokenSetReference = {
    new AlexTokenSetReference(this)
  }

  override def getReferences: Array[PsiReference] = {
    Array(getReference)
  }

  override def getName: String = {
    getText
  }

  override def getNameIdentifier: PsiElement = {
    this
  }

  override def setName(s: String): PsiElement = throw new IncorrectOperationException("")
}
