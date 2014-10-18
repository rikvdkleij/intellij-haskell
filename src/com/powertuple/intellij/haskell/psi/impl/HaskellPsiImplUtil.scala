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
import com.intellij.openapi.util.Condition
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.util.ArrayUtil
import com.powertuple.intellij.haskell.HaskellIcons
import com.powertuple.intellij.haskell.psi._

import scala.collection.JavaConversions._

object HaskellPsiImplUtil {

  def getName(qvar: HaskellQvar): String = {
    qvar.getIdentifierElement.getName
  }

  def getIdentifierElement(qvar: HaskellQvar): HaskellNamedElement = {
    Option(qvar.getQvarId).orElse(Option(qvar.getQvarSym)).get
  }


  def getName(qcon: HaskellQcon): String = {
    qcon.getIdentifierElement.getName
  }

  def getIdentifierElement(qcon: HaskellQcon): HaskellNamedElement = {
    Option(qcon.getQconId).orElse(Option(qcon.getGconSym)).get
  }

  def getIdentifierElement(op: HaskellOp): HaskellNamedElement = {
    Option(op.getQconop).map(_.getIdentifierElement).orElse(Option(op.getQvarop).map(_.getIdentifierElement)).get
  }


  def getName(qvarop: HaskellQvarop): String = {
    qvarop.getIdentifierElement.getName
  }

  def getIdentifierElement(qvarop: HaskellQvarop): HaskellNamedElement = {
    Option(qvarop.getQvarId).orElse(Option(qvarop.getQvarSym)).get
  }


  def getName(qconop: HaskellQconop): String = {
    qconop.getIdentifierElement.getName
  }

  def getIdentifierElement(qconop: HaskellQconop): HaskellNamedElement = {
    Option(qconop.getQconId).orElse(Option(qconop.getGconSym)).get
  }


  def getName(qvarId: HaskellQvarId): String = {
    qvarId.getNameIdentifier.getText
  }

  def getNameIdentifier(qvarId: HaskellQvarId): PsiElement = {
    qvarId
  }

  def setName(qvarId: HaskellQvarId, newName: String): PsiElement = {
    val newQVarId = HaskellElementFactory.createQvarId(qvarId.getProject, newName)
    qvarId.getNode.getTreeParent.replaceChild(qvarId.getNode, newQVarId)
    qvarId
  }


  def getName(qvarSym: HaskellQvarSym): String = {
    qvarSym.getText
  }

  def getNameIdentifier(qvarSym: HaskellQvarSym): PsiElement = {
    qvarSym
  }

  def setName(qvarSym: HaskellQvarSym, newName: String): PsiElement = {
    val newQvarSym = HaskellElementFactory.createQvarSym(qvarSym.getProject, newName)
    qvarSym.getNode.getTreeParent.replaceChild(qvarSym.getNode, newQvarSym)
    qvarSym
  }


  def getName(qconId: HaskellQconId): String = {
    qconId.getText
  }

  def getNameIdentifier(qconId: HaskellQconId): PsiElement = {
    qconId
  }

  def setName(qconId: HaskellQconId, newName: String): PsiElement = {
    val newQconId = HaskellElementFactory.createQconId(qconId.getProject, newName)
    qconId.getNode.getTreeParent.replaceChild(qconId.getNode, newQconId)
    qconId
  }


  def getName(gconSym: HaskellGconSym): String = {
    gconSym.getText
  }

  def getNameIdentifier(gconSym: HaskellGconSym): PsiElement = {
    gconSym
  }

  def setName(gconSym: HaskellGconSym, newName: String): PsiElement = {
    val newGconSym = HaskellElementFactory.createGconSym(gconSym.getProject, newName)
    gconSym.getNode.getTreeParent.replaceChild(gconSym.getNode, newGconSym)
    gconSym
  }


  def getReference(namedElement: HaskellNamedElement): PsiReference = {
    ArrayUtil.getFirstElement(ReferenceProvidersRegistry.getReferencesFromProviders(namedElement))
  }

  private final val declarationElementCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellDeclarationElement => true
        case _ => false
      }
    }
  }

  private abstract class HaskellItemPresentation(haskellElement: PsiElement) extends ItemPresentation {
    override def getLocationString: String = {
      val node = haskellElement.getContainingFile.getNode.getPsi
      Option(PsiTreeUtil.findChildOfType(node, classOf[HaskellModuleDeclaration])).map(_.getModuleName).getOrElse("")
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
        case _: HaskellTypeSignature => TypeSignature
        case _: HaskellForeignDeclaration => Foreign
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _ => HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {
    val declarationElement = Option(PsiTreeUtil.findFirstParent(namedElement, declarationElementCondition)).getOrElse(namedElement)

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

    (declarationElement match {
      case td: HaskellTypeDeclaration => removeCommentsAndAfterWhereOrEqual(td)
      case nt: HaskellNewtypeDeclaration => removeCommentsAndAfterWhereOrEqual(nt)
      case cd: HaskellClassDeclaration => removeCommentsAndAfterWhereOrEqual(cd)
      case id: HaskellInstanceDeclaration => removeCommentsAndAfterWhereOrEqual(id)
      case ts: HaskellTypeSignature => removeCommentsAndAfterWhereOrEqual(ts)
      case tf: HaskellTypeFamilyDeclaration => removeCommentsAndAfterWhereOrEqual(tf)
      case de => removeComments(de)
    }).map(_.getText.trim).mkString(" ").replaceAll("\\s+", " ")
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getIdentifierElements.map(_.getName).mkString(" ")
  }

  def getIdentifierElements(typeSignature: HaskellTypeSignature): Seq[HaskellNamedElement] = {
    Option(typeSignature.getVars).map(_.getQvarList.map(_.getIdentifierElement).toSeq).
        orElse(Option(typeSignature.getOps).map(_.getOpList.map(_.getIdentifierElement).toSeq)).
        getOrElse(Seq())
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    dataDeclaration.getSimpletype.getIdentifierElements ++ Option(dataDeclaration.getConstr1List).map(_.flatMap(c => Option(c.getQcon.getIdentifierElement))).toSeq.flatten ++
        Option(dataDeclaration.getConstr2List).map(_.flatMap(c => Option(c.getQconop.getIdentifierElement))).toSeq.flatten ++
        Option(dataDeclaration.getConstr3List).map(_.flatMap(c => Option(c.getQcon.getIdentifierElement))).toSeq.flatten ++
        Option(dataDeclaration.getConstr4List).map(_.flatMap(c => Option(c.getQconId))).toSeq.flatten
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    newtypeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    Seq(classDeclaration.getQconId) ++
        Option(classDeclaration.getCdeclList).map(_.map(c => Option(c.getTypeSignature)).flatten.flatMap(_.getIdentifierElements)).getOrElse(Seq())
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    val inst = instanceDeclaration.getInst
    Seq(instanceDeclaration.getQconId) ++
        Option(inst.getGtycon).map(g => Option(g.getQconId).toSeq).getOrElse(Seq()) ++
        Option(instanceDeclaration.getIdeclList).map(_.map(_.getQvarIdList.headOption).flatten).getOrElse(Seq())
  }

  def getIdentifierElements(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): Seq[HaskellNamedElement] = {
    val familyType = typeFamilyDeclaration.getTypeFamilyType
    Option(familyType.getTypeFamilyType1List).map(_.map(_.getQconId)).
        orElse(Option(familyType.getTypeFamilyType2List).map(_.map(_.getQvarSym))).
        orElse(Option(familyType.getVarsList).map(_.flatMap(_.getQvarList.map(_.getIdentifierElement)))).
        getOrElse(Seq())
  }

  def getIdentifierElements(derivingDeclaration: HaskellDerivingDeclaration): Seq[HaskellNamedElement] = {
    Seq(derivingDeclaration.getQconId)
  }

  def getIdentifierElements(typeInstanceDeclaration: HaskellTypeInstanceDeclaration): Seq[HaskellNamedElement] = {
    Option(PsiTreeUtil.findChildOfType(typeInstanceDeclaration, classOf[HaskellQconId])).toSeq
  }

  def getIdentifierElements(simpleType: HaskellSimpletype): Seq[HaskellNamedElement] = {
    Option(simpleType.getQconId).orElse(Option(simpleType.getQvarSym)).orElse(Option(simpleType.getGconSym)).toSeq
  }

  def getModuleName(importDeclaration: HaskellImportDeclaration): String = {
    importDeclaration.getImportModule.getQconId.getName
  }

  def getModuleName(moduleDeclaration: HaskellModuleDeclaration): String = {
    Option(moduleDeclaration.getQconId).map(_.getName).getOrElse("Undefined module")
  }
}