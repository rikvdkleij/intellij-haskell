package intellij.haskell.alex.lang.psi.impl

import java.util.Objects

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import intellij.haskell.alex.lang.psi._

/**
  * @author ice1000
  * @param node ast node
  */
abstract class AlexTokenSetIdMixin(node: ASTNode) extends AlexElementImpl(node)
  with AlexTokenSetId with PsiNameIdentifierOwner with PsiPolyVariantReference {
  override def resolve(): PsiElement = {
    val r = multiResolve(false)
    if (r.isEmpty) null
    else r(0).getElement
  }

  override def multiResolve(b: Boolean): Array[ResolveResult] = {
    val declarations = getAlexDeclarationsSection
    if (declarations == null) return Array()
    declarations
      .getDeclarationList
      .stream
      .map[AlexTokenSetDeclaration](_.getTokenSetDeclaration)
      .filter(o => Objects.nonNull(o))
      .filter(o => o.getTokenSetId.getText eq getText)
      .toArray(s => new Array[ResolveResult](s))
  }

  private def getAlexDeclarationsSection: AlexDeclarationsSection = {
    val file = getElement.getContainingFile
    if (file == null) return null
    val declarations = PsiTreeUtil.findChildOfType(file, classOf[AlexDeclarationsSection])
    if (declarations == null) return null
    declarations
  }

  override def getVariants: Array[AnyRef] = {
    val declarations = getAlexDeclarationsSection
    if (declarations == null) return Array()
    declarations
      .getDeclarationList
      .stream
      .map[AlexTokenSetDeclaration](_.getTokenSetDeclaration)
      .filter(o => Objects.nonNull(o))
      .map[String](o => o.getTokenSetId.getText)
      .toArray(s => new Array[AnyRef](s))
  }

  override def getElement: PsiElement = {
    this
  }

  private val range = new TextRange(0, getTextLength)

  override def getRangeInElement: TextRange = {
    range
  }

  override def getReference: AlexTokenSetIdMixin = {
    this
  }

  override def getReferences: Array[PsiReference] = {
    Array(getReference)
  }

  override def getName: String = {
    getText
  }

  override def getCanonicalText: String = {
    getText
  }

  override def isSoft: Boolean = {
    true
  }

  override def isReferenceTo(psiElement: PsiElement): Boolean = {
    psiElement == this
  }

  override def handleElementRename(s: String): PsiElement = throw new IncorrectOperationException("Unsupported")

  override def bindToElement(psiElement: PsiElement): PsiElement = throw new IncorrectOperationException("Unsupported")

  override def getNameIdentifier: PsiElement = {
    this
  }

  override def setName(s: String): PsiElement = throw new IncorrectOperationException("Unsupported")
}
