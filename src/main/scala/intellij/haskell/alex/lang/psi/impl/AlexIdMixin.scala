package intellij.haskell.alex.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import intellij.haskell.alex.lang.psi._

import scala.collection.mutable

/**
  * @author ice1000
  * @param node ast node
  */
abstract class AlexIdMixin(node: ASTNode) extends AlexElementImpl(node)
  with PsiNameIdentifierOwner with PsiPolyVariantReference {
  override def resolve(): PsiElement = {
    val r = multiResolve(false)
    if (r.isEmpty) null
    else r(0).getElement
  }

  override def multiResolve(b: Boolean): Array[ResolveResult] = {
    val declarations = getAlexDeclarationsSection
    if (declarations == null) return Array()
    val variants = mutable.MutableList[ResolveResult]()
    declarations.getDeclarationList.forEach { decl =>
      val tokenSet = decl.getTokenSetDeclaration
      if (tokenSet != null && (tokenSet.getTokenSetId.getText equals getText))
        variants += new PsiElementResolveResult(tokenSet.getTokenSetId)
      val rules = decl.getRuleDeclaration
      if (rules != null && (rules.getRuleId.getText equals getText))
        variants += new PsiElementResolveResult(rules.getRuleId)
    }
    variants.toArray
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
    val variants = mutable.MutableList[AnyRef]()
    declarations.getDeclarationList.forEach { decl =>
      val tokenSet = decl.getTokenSetDeclaration
      if (tokenSet != null) variants += tokenSet.getTokenSetId.getText
      val rules = decl.getRuleDeclaration
      if (rules != null) variants += rules.getRuleId.getText
    }
    variants.toArray
  }

  override def getElement: PsiElement = {
    this
  }

  private val range = new TextRange(0, getTextLength)

  override def getRangeInElement: TextRange = {
    range
  }

  override def getReference: AlexIdMixin = {
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
