/*
 * Copyright 2016 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.psi.impl

import javax.swing._

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.{LocalSearchScope, SearchScope}
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.{PsiTreeUtil, PsiUtilCore}
import com.intellij.psi.{PsiElement, PsiReference, TokenType}
import com.intellij.util.ArrayUtil
import intellij.haskell.HaskellIcons
import intellij.haskell.navigate._
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.util.HaskellElementCondition.DeclarationElementCondition

import scala.collection.JavaConversions._

object HaskellPsiImplUtil {

  def getName(qvar: HaskellQvar): String = {
    qvar.getText
  }

  def getIdentifierElement(qvar: HaskellQvar): HaskellNamedElement = {
    Option(qvar.getVarId).
      orElse(Option(qvar.getVarSym)).
      orElse(Option(qvar.getQvarId).map(_.getVarId)).
      orElse(Option(qvar.getQvarSym.getVarSym)).
      getOrElse(throw new Exception(s"Identifier for $qvar should exist"))
  }

  def getQualifier(qvar: HaskellQvar): Option[String] = {
    Option(qvar.getQvarId).map(_.getQualifier.getName).
      orElse(Option(qvar.getQvarSym).map(_.getQualifier.getName))
  }


  def getName(qvarId: HaskellQvarId): String = {
    qvarId.getText
  }

  def getIdentifierElement(qvarId: HaskellQvarId): HaskellNamedElement = {
    qvarId.getVarId
  }


  def getName(qcon: HaskellQcon): String = {
    qcon.getText
  }

  def getIdentifierElement(qcon: HaskellQcon): HaskellNamedElement = {
    Option(qcon.getConId).
      orElse(Option(qcon.getConSym)).
      orElse(Option(qcon.getQconId).map(_.getIdentifierElement)).
      orElse(Option(qcon.getGconSym.getIdentifierElement)).
      getOrElse(throw new Exception(s"Identifier for $qcon should exist"))

  }

  private def getQualifierForQConId(qconId: HaskellQconId) = {
    qconId.getName.substring(0, qconId.getName.indexOf(qconId.getIdentifierElement.getName) - 1)
  }

  def getQualifier(qcon: HaskellQcon): Option[String] = {
    Option(qcon.getQconId).map(getQualifierForQConId).
      orElse(Option(qcon.getGconSym).flatMap(qs => Option(qs.getQconSym).map(_.getQualifier.getName)))
  }


  def getName(qconId: HaskellQconId): String = {
    qconId.getText
  }

  def getIdentifierElement(qconId: HaskellQconId): HaskellNamedElement = {
    qconId.getConId
  }


  def getName(qvarop: HaskellQvarOp): String = {
    qvarop.getText
  }

  def getIdentifierElement(qvarOp: HaskellQvarOp): HaskellNamedElement = {
    Option(qvarOp.getVarId).
      orElse(Option(qvarOp.getVarSym)).
      orElse(Option(qvarOp.getQvarId).map(_.getVarId)).
      orElse(Option(qvarOp.getQvarSym.getVarSym)).
      getOrElse(throw new Exception(s"Identifier for $qvarOp should exist"))
  }

  def getQualifier(qvarOp: HaskellQvarOp): Option[String] = {
    Option(qvarOp.getQvarId).map(_.getQualifier.getName).
      orElse(Option(qvarOp.getQvarSym).map(_.getQualifier.getName))
  }


  def getName(qconop: HaskellQconOp): String = {
    qconop.getText
  }

  def getIdentifierElement(qconOp: HaskellQconOp): HaskellNamedElement = {
    Option(qconOp.getConId).
      orElse(Option(qconOp.getConSym)).
      orElse(Option(qconOp.getQconId).map(_.getIdentifierElement)).
      orElse(Option(qconOp.getGconSym.getIdentifierElement)).
      getOrElse(throw new Exception(s"Identifier for $qconOp should exist"))
  }

  def getQualifier(qconOp: HaskellQconOp): Option[String] = {
    Option(qconOp.getQconId).map(getQualifierForQConId).
      orElse(Option(qconOp.getGconSym).flatMap(qs => Option(qs.getQconSym).map(_.getQualifier.getName)))
  }


  def getName(op: HaskellOp): String = {
    op.getText
  }

  def getIdentifierElement(op: HaskellOp): HaskellNamedElement = {
    Option(op.getQvarOp).map(_.getIdentifierElement).
      orElse(Option(op.getQconOp.getIdentifierElement)).
      getOrElse(throw new Exception(s"Identifier for $op should exist"))
  }


  def getName(gconSym: HaskellGconSym): String = {
    gconSym.getText
  }

  def getIdentifierElement(gconSym: HaskellGconSym): HaskellNamedElement = {
    Option(gconSym.getQconSym).map(_.getConSym).
      orElse(Option(gconSym.getConSym)).
      getOrElse(throw new Exception(s"Identifier for $gconSym should exist"))
  }


  def getName(modId: HaskellModId): String = {
    modId.getText
  }

  def getNameIdentifier(modId: HaskellModId): HaskellNamedElement = {
    modId
  }

  def setName(modId: HaskellModId, newName: String): PsiElement = {
    val newModId = HaskellElementFactory.createModId(modId.getProject, newName)
    modId.getNode.getTreeParent.replaceChild(modId.getNode, newModId)
    modId
  }

  def getName(varId: HaskellVarId): String = {
    varId.getText
  }

  def getNameIdentifier(varId: HaskellVarId): HaskellNamedElement = {
    varId
  }

  def setName(varId: HaskellVarId, newName: String): PsiElement = {
    val newVarId = HaskellElementFactory.createVarId(varId.getProject, newName)
    varId.getNode.getTreeParent.replaceChild(varId.getNode, newVarId)
    varId
  }


  def getName(varSym: HaskellVarSym): String = {
    varSym.getText
  }

  def getNameIdentifier(varSym: HaskellVarSym): HaskellNamedElement = {
    varSym
  }

  def setName(varSym: HaskellVarSym, newName: String): PsiElement = {
    val newVarSym = HaskellElementFactory.createVarSym(varSym.getProject, newName)
    varSym.getNode.getTreeParent.replaceChild(varSym.getNode, newVarSym)
    varSym
  }


  def getName(conId: HaskellConId): String = {
    conId.getText
  }

  def getNameIdentifier(conId: HaskellConId): HaskellNamedElement = {
    conId
  }

  def setName(conId: HaskellConId, newName: String): PsiElement = {
    val newConId = HaskellElementFactory.createConId(conId.getProject, newName)
    conId.getNode.getTreeParent.replaceChild(conId.getNode, newConId)
    conId
  }


  def getName(conSym: HaskellConSym): String = {
    conSym.getText
  }

  def getNameIdentifier(conSym: HaskellConSym): HaskellNamedElement = {
    conSym
  }

  def setName(conSym: HaskellConSym, newName: String): PsiElement = {
    val newConSym = HaskellElementFactory.createConSym(conSym.getProject, newName)
    conSym.getNode.getTreeParent.replaceChild(conSym.getNode, newConSym)
    conSym
  }


  def getName(qualifier: HaskellQualifier): String = {
    qualifier.getText
  }

  def getNameIdentifier(qualifier: HaskellQualifier): HaskellNamedElement = {
    qualifier
  }

  def setName(qualifier: HaskellQualifier, newName: String): PsiElement = {
    val newQualifier = HaskellElementFactory.createQualifier(qualifier.getProject, newName)
    qualifier.getNode.getTreeParent.replaceChild(qualifier.getNode, newQualifier)
    qualifier
  }


  def getReference(element: PsiElement): PsiReference = {
    ArrayUtil.getFirstElement(ReferenceProvidersRegistry.getReferencesFromProviders(element))
  }

  private abstract class HaskellItemPresentation(haskellElement: PsiElement) extends ItemPresentation {

    def getLocationString: String = {
      val node = haskellElement.getContainingFile.getNode.getPsi
      Option(PsiTreeUtil.findChildOfType(node, classOf[HaskellModuleDeclaration])).map(_.getModId.getName).getOrElse("")
    }

    def getIcon(unused: Boolean): Icon = {
      import intellij.haskell.HaskellIcons._
      haskellElement match {
        case _: HaskellTypeDeclaration => Type
        case _: HaskellDataDeclaration => Data
        case _: HaskellNewtypeDeclaration => NewType
        case _: HaskellClassDeclaration => Class
        case _: HaskellInstanceDeclaration => Instance
        case _: HaskellDefaultDeclaration => Default
        case _: HaskellTypeSignature => TypeSignature
        case _: HaskellForeignDeclaration => Foreign
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _: HaskellModuleDeclaration => Module
        case _ => HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {

    new HaskellItemPresentation(namedElement) {
      val declarationElement = Option(PsiTreeUtil.findFirstParent(namedElement, DeclarationElementCondition).asInstanceOf[HaskellDeclarationElement])

      def getPresentableText: String = {
        declarationElement.map(e =>
          if (e.getIdentifierElements.map(_.getName).contains(namedElement.getName)) {
            getDeclarationInfo(e)
          } else {
            namedElement.getName + " in " + getDeclarationInfo(e)
          }).getOrElse(namedElement.getName)
      }

      override def getIcon(unused: Boolean): Icon = {
        declarationElement match {
          case de: HaskellDeclarationElement => super.getIcon(unused)
          case _ => HaskellIcons.HaskellSmallBlueLogo
        }
      }
    }
  }

  def getPresentation(declarationElement: HaskellDeclarationElement): ItemPresentation = {

    new HaskellItemPresentation(declarationElement) {

      def getPresentableText: String = {
        getDeclarationInfo(declarationElement)
      }
    }
  }

  private def getDeclarationInfo(declarationElement: HaskellDeclarationElement): String = {
    val removeAfter = (nodes: Array[ASTNode], tokens: Seq[IElementType]) => nodes.takeWhile(e => !tokens.contains(e.getElementType))
    val removeInside = (de: HaskellCompositeElement, tokens: Seq[IElementType]) => de.getNode.getChildren(null).filterNot(e => tokens.contains(e.getElementType))
    val removeCommentsAndPragmas = (de: HaskellCompositeElement) => removeInside(de, Seq(HS_COMMENT, HS_NCOMMENT, HS_OVERLAP_PRAGMA, HS_CTYPE_PRAGMA, HS_UNPACK_PRAGMA, HS_NOUNPACK_PRAGMA, HS_INLINE_PRAGMA, HS_INLINABLE_PRAGMA, HS_NOINLINE_PRAGMA, HS_SPECIALIZE_PRAGMA))
    val removeCommentsAndPragmasAndAfterWhereOrEqual = (de: HaskellDeclarationElement) => removeAfter(removeCommentsAndPragmas(de), Seq(HS_WHERE, HS_EQUAL))
    val removeCommentsAndPragmasAndAfterModId = (de: HaskellDeclarationElement) => removeAfter(removeCommentsAndPragmas(de), Seq(HS_EXPORTS, HS_WHERE))

    (declarationElement match {
      case md: HaskellModuleDeclaration => removeCommentsAndPragmasAndAfterModId(md)
      case td: HaskellTypeDeclaration => removeCommentsAndPragmas(td)
      case nt: HaskellNewtypeDeclaration => removeCommentsAndPragmasAndAfterWhereOrEqual(nt)
      case cd: HaskellClassDeclaration => removeCommentsAndPragmasAndAfterWhereOrEqual(cd)
      case id: HaskellInstanceDeclaration => removeCommentsAndPragmasAndAfterWhereOrEqual(id)
      case ts: HaskellTypeSignature => removeCommentsAndPragmas(ts)
      case tf: HaskellDataDeclaration => removeCommentsAndPragmasAndAfterWhereOrEqual(tf)
      case tf: HaskellTypeFamilyDeclaration => removeCommentsAndPragmasAndAfterWhereOrEqual(tf)
      case de => removeCommentsAndPragmas(de)
    }).map(e => if (e.getElementType == TokenType.WHITE_SPACE) " " else e.getText.trim).mkString(" ").replaceAll("\\s+", " ")
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getPresentableText
  }


  def getIdentifierElements(typeSignature: HaskellTypeSignature): Seq[HaskellNamedElement] = {
    Option(typeSignature.getVars).map(_.getQvarList.map(_.getIdentifierElement)).
      orElse(Option(typeSignature.getOps).map(_.getOpList.map(_.getIdentifierElement))).
      getOrElse(Seq())
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    dataDeclaration.getSimpletypeList.flatMap(_.getIdentifierElements) ++
      Option(dataDeclaration.getConstr1List).map(_.flatMap(c => Seq(c.getQcon.getIdentifierElement) ++ c.getFielddeclList.flatMap(_.getVars.getQvarList.map(_.getIdentifierElement)))).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr2List).map(_.map(c => c.getQconOp.getIdentifierElement)).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr3List).map(_.map(c => c.getQcon.getIdentifierElement)).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr4List).map(_.flatMap(c => Seq(c.getGconSym.getIdentifierElement, c.getQcon.getIdentifierElement))).getOrElse(Seq())
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    newtypeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    Seq(classDeclaration.getQcon.getIdentifierElement)
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    val inst = instanceDeclaration.getInst
    Seq(instanceDeclaration.getQcon.getIdentifierElement) ++
      Option(inst.getGtycon).flatMap(g => Option(g.getQcon).map(_.getIdentifierElement)).toSeq ++
      Option(inst.getInstvarList).map(_.flatMap(iv => Option(iv.getGconSym).map(_.getIdentifierElement).
        orElse(Option(iv.getQcon).map(_.getIdentifierElement)))).getOrElse(Seq())
  }

  def getIdentifierElements(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): Seq[HaskellNamedElement] = {
    val familyType = typeFamilyDeclaration.getTypeFamilyType
    Option(familyType.getTypeFamilyType1List).map(_.map(_.getQcon.getIdentifierElement)).
      orElse(Option(familyType.getTypeFamilyType2List).map(_.map(_.getQvarOp.getIdentifierElement))).
      orElse(Option(familyType.getVarsList).map(_.flatMap(_.getQvarList.map(_.getIdentifierElement)))).
      getOrElse(Seq())
  }

  def getIdentifierElements(derivingDeclaration: HaskellDerivingDeclaration): Seq[HaskellNamedElement] = {
    Seq(derivingDeclaration.getQcon.getIdentifierElement)
  }

  def getIdentifierElements(typeInstanceDeclaration: HaskellTypeInstanceDeclaration): Seq[HaskellNamedElement] = {
    Option(PsiTreeUtil.findChildOfType(typeInstanceDeclaration, classOf[HaskellQcon])).map(_.getIdentifierElement).toSeq
  }

  def getIdentifierElements(simpleType: HaskellSimpletype): Seq[HaskellNamedElement] = {
    Option(simpleType.getQcon).map(_.getIdentifierElement).
      orElse(Option(simpleType.getQconOp).map(_.getIdentifierElement)).
      orElse(Option(simpleType.getQvarOp).map(_.getIdentifierElement)).
      orElse(Option(simpleType.getGconSym).map(_.getIdentifierElement)).toSeq
  }

  def getIdentifierElements(defaultDeclaration: HaskellDefaultDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(foreignDeclaration: HaskellForeignDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(moduleDeclaration: HaskellModuleDeclaration): Seq[HaskellNamedElement] = {
    Seq(moduleDeclaration.getModId)
  }


  def getModuleName(importDeclaration: HaskellImportDeclaration): String = {
    importDeclaration.getImportModule.getModId.getName
  }

  def getModuleName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getLocationString
  }


  def getDataTypeConstructor(dataConstructorDeclaration: HaskellDataConstructorDeclarationElement): HaskellNamedElement = {
    dataConstructorDeclaration.getIdentifierElements.head
  }

  def getUseScope(namedElement: HaskellNamedElement): SearchScope = {
    val resolvedResult = Option(namedElement.getReference.asInstanceOf[HaskellReference]).flatMap(_.multiResolve(false).headOption)
    resolvedResult match {
      case Some(_: HaskellProjectResolveResult) | Some(_: HaskellLibraryResolveResult) | Some(_: HaskellFileResolveResult) => ResolveScopeManager.getElementUseScope(namedElement)
      case Some(lrr: HaskellLocalResolveResult) =>
        Option(PsiTreeUtil.getParentOfType(namedElement, classOf[HaskellExpression])) match {
          case Some(e) if findFirstChild(e).exists(_.getIdentifierElement.getName == namedElement.getName) => ResolveScopeManager.getElementUseScope(namedElement)
          case Some(e) =>
            val target = lrr.getElement
            val qVarConOpElements = PsiTreeUtil.findChildrenOfType(e, classOf[HaskellQVarConOpElement])
            val references = qVarConOpElements.filter(e => e.getIdentifierElement.getName == namedElement.getName && Option(e.getIdentifierElement.getReference).flatMap(r => Option(r.resolve)).exists(_.getTextOffset == target.getTextOffset))
            if (references.isEmpty) {
              ResolveScopeManager.getElementUseScope(namedElement)
            } else {
              new LocalSearchScope(PsiUtilCore.toPsiElementArray(references))
            }
          case None => ResolveScopeManager.getElementUseScope(namedElement)
        }
      case _ => ResolveScopeManager.getElementUseScope(namedElement)
    }
  }

  private def findFirstChild(e: HaskellExpression) = {
    Option(PsiTreeUtil.findChildOfType(e, classOf[HaskellQVarConOpElement]))
  }
}