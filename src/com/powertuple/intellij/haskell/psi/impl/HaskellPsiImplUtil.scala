/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.psi.impl

import javax.swing._

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.{LocalSearchScope, SearchScope}
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.{PsiTreeUtil, PsiUtilCore}
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.util.ArrayUtil
import com.powertuple.intellij.haskell.HaskellIcons
import com.powertuple.intellij.haskell.navigate.{HaskellFileResolveResult, HaskellGlobalResolveResult, HaskellLocalResolveResult, HaskellReference}
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.HaskellElementCondition

import scala.collection.JavaConversions._

object HaskellPsiImplUtil {

  def getName(qvar: HaskellQvar): String = {
    qvar.getText
  }

  def getIdentifierElement(qvar: HaskellQvar): HaskellNamedElement = {
    Option(qvar.getVarId).
        orElse(Option(qvar.getVarSym)).
        orElse(Option(qvar.getVarDotSym)).
        orElse(Option(qvar.getQvarId).map(_.getVarId)).
        orElse(Option(qvar.getQvarSym).map(_.getVarSym)).
        orElse(Option(qvar.getQvarDotSym).map(_.getVarDotSym)).
        getOrElse(throw new Exception(s"Identifier for $qvar should exist"))
  }

  def getQualifier(qvar: HaskellQvar): Option[String] = {
    Option(qvar.getQvarDotSym).map(_.getQualifier.getName).
        orElse(Option(qvar.getQvarId).map(_.getQualifier.getName)).
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
        orElse(Option(qcon.getGconSym).map(_.getIdentifierElement)).
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
        orElse(Option(qvarOp.getVarDotSym)).
        orElse(Option(qvarOp.getQvarId).map(_.getVarId)).
        orElse(Option(qvarOp.getQvarSym).map(_.getVarSym)).
        orElse(Option(qvarOp.getQvarDotSym).map(_.getVarDotSym)).
        getOrElse(throw new Exception(s"Identifier for $qvarOp should exist"))
  }

  def getQualifier(qvarOp: HaskellQvarOp): Option[String] = {
    Option(qvarOp.getQvarDotSym).map(_.getQualifier.getName).
        orElse(Option(qvarOp.getQvarId).map(_.getQualifier.getName)).
        orElse(Option(qvarOp.getQvarSym).map(_.getQualifier.getName))
  }


  def getName(qconop: HaskellQconOp): String = {
    qconop.getText
  }

  def getIdentifierElement(qconOp: HaskellQconOp): HaskellNamedElement = {
    Option(qconOp.getConId).
        orElse(Option(qconOp.getConSym)).
        orElse(Option(qconOp.getQconId).map(_.getIdentifierElement)).
        orElse(Option(qconOp.getGconSym).map(_.getIdentifierElement)).
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
        orElse(Option(op.getQconOp).map(_.getIdentifierElement)).
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


  def getName(varDotSym: HaskellVarDotSym): String = {
    varDotSym.getText
  }

  def getNameIdentifier(varDotSym: HaskellVarDotSym): HaskellNamedElement = {
    varDotSym
  }

  def setName(varDotSym: HaskellVarDotSym, newName: String): PsiElement = {
    val newVarDotSym = HaskellElementFactory.createVarDotSym(varDotSym.getProject, newName)
    varDotSym.getNode.getTreeParent.replaceChild(varDotSym.getNode, newVarDotSym)
    varDotSym
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
    override def getLocationString: String = {
      val node = haskellElement.getContainingFile.getNode.getPsi
      Option(PsiTreeUtil.findChildOfType(node, classOf[HaskellModuleDeclaration])).map(_.getModId.getName).getOrElse("")
    }

    def getIcon(unused: Boolean): Icon = {
      import com.powertuple.intellij.haskell.HaskellIcons._
      haskellElement match {
        case _: HaskellTypeDeclaration => Type
        case _: HaskellDataDeclaration => Data
        case _: HaskellNewtypeDeclaration => NewType
        case _: HaskellClassDeclaration => Class
        case _: HaskellInstanceDeclaration => Instance
        case _: HaskellDefaultDeclaration => Default
        case _: HaskellTypeSignatureDeclaration => TypeSignature
        case _: HaskellForeignDeclaration => Foreign
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _: HaskellModuleDeclaration => Module
        case _ => HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {
    val declarationElement = Option(PsiTreeUtil.findFirstParent(namedElement, HaskellElementCondition.DeclarationElementCondition)).getOrElse(namedElement)

    new HaskellItemPresentation(declarationElement) {

      def getPresentableText: String = {
        declarationElement match {
          case de: HaskellDeclarationElement => getDeclarationInfo(de)
          case _ => namedElement.getName
        }
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
    val removeInside = (de: HaskellDeclarationElement, tokens: Seq[IElementType]) => de.getNode.getChildren(null).filterNot(e => tokens.contains(e.getElementType))
    val removeComments = (de: HaskellDeclarationElement) => removeInside(de, Seq(HaskellTypes.HS_COMMENT, HaskellTypes.HS_NCOMMENT))
    val removeCommentsAndAfterWhereOrEqual = (de: HaskellDeclarationElement) => removeAfter(removeComments(de), Seq(HaskellTypes.HS_WHERE, HaskellTypes.HS_EQUAL))
    val removeCommentsAndAfterModId = (de: HaskellDeclarationElement) => removeAfter(removeComments(de), Seq(HaskellTypes.HS_EXPORTS, HaskellTypes.HS_WHERE))

    (declarationElement match {
      case md: HaskellModuleDeclaration => removeCommentsAndAfterModId(md)
      case td: HaskellTypeDeclaration => removeCommentsAndAfterWhereOrEqual(td)
      case nt: HaskellNewtypeDeclaration => removeCommentsAndAfterWhereOrEqual(nt)
      case cd: HaskellClassDeclaration => removeCommentsAndAfterWhereOrEqual(cd)
      case id: HaskellInstanceDeclaration => removeCommentsAndAfterWhereOrEqual(id)
      case ts: HaskellTypeSignatureDeclaration => removeCommentsAndAfterWhereOrEqual(ts)
      case tf: HaskellDataDeclaration => removeCommentsAndAfterWhereOrEqual(tf)
      case tf: HaskellTypeFamilyDeclaration => removeCommentsAndAfterWhereOrEqual(tf)
      case de => removeComments(de)
    }).map(_.getText.trim).mkString(" ").replaceAll("\\s+", " ")
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getPresentableText
  }

  def getIdentifierElements(typeSignature: HaskellTypeSignatureDeclaration): Seq[HaskellNamedElement] = {
    Option(typeSignature.getVars).map(_.getQvarList.map(_.getIdentifierElement)).
        orElse(Option(typeSignature.getOps).map(_.getOpList.map(_.getIdentifierElement))).
        getOrElse(Seq())
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    dataDeclaration.getSimpletype.getIdentifierElements ++
        Option(dataDeclaration.getConstr1List).map(_.map(c => c.getQcon.getIdentifierElement)).getOrElse(Seq()) ++
        Option(dataDeclaration.getConstr2List).map(_.map(c => c.getQconOp.getIdentifierElement)).getOrElse(Seq()) ++
        Option(dataDeclaration.getConstr3List).map(_.map(c => c.getQcon.getIdentifierElement)).getOrElse(Seq()) ++
        Option(dataDeclaration.getConstr4List).map(_.flatMap(c => Seq(c.getGconSym.getIdentifierElement, c.getQcon.getIdentifierElement))).getOrElse(Seq()) ++
        Option(dataDeclaration.getDataDeclarationDeriving).map(e => PsiTreeUtil.findChildrenOfType(e, classOf[HaskellQcon]).map(_.getIdentifierElement)).getOrElse(Seq())
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    newtypeDeclaration.getSimpletype.getIdentifierElements ++ Seq(newtypeDeclaration.getNewconstr.getQcon.getIdentifierElement)
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    Seq(classDeclaration.getQcon.getIdentifierElement) ++
        Option(classDeclaration.getCdeclList).map(_.flatMap(c => Option(c.getTypeSignatureDeclaration)).flatMap(_.getIdentifierElements)).getOrElse(Seq())
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    val inst = instanceDeclaration.getInst
    Seq(instanceDeclaration.getQcon.getIdentifierElement) ++
        Option(inst.getGtycon).flatMap(g => Option(g.getQcon).map(_.getIdentifierElement)).toSeq ++
        Option(inst.getInstvarList).map(_.map(iv => Option(iv.getGconSym).map(_.getIdentifierElement).
            orElse(Option(iv.getQcon).map(_.getIdentifierElement))).flatten).getOrElse(Seq())
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
        orElse(Option(simpleType.getQvar).map(_.getIdentifierElement)).
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
    importDeclaration.getImportModule.getModId.getText
  }

  def getModuleName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getLocationString
  }

  def getSimpleType(dataConstructorDeclaration: HaskellDataConstructorDeclarationElement): HaskellNamedElement = {
    dataConstructorDeclaration.getIdentifierElements.head
  }

  def getUseScope(namedElement: HaskellNamedElement): SearchScope = {
    val resolvedResult = Option(namedElement.getReference.asInstanceOf[HaskellReference]).flatMap(_.multiResolve(false).headOption)
    resolvedResult match {
      case Some(_: HaskellGlobalResolveResult) | Some(_: HaskellFileResolveResult) => ResolveScopeManager.getElementUseScope(namedElement)
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