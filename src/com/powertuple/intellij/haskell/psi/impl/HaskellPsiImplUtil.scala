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

  def getName(haskellQvar: HaskellQvar): String = {
    haskellQvar.getNameIdentifier.getText
  }

  def getNameIdentifier(haskellQvar: HaskellQvar): PsiElement = {
    haskellQvar
  }

  def setName(haskellQvar: HaskellQvar, newName: String): PsiElement = {
    val newHaskellVar = HaskellElementFactory.createQvar(haskellQvar.getProject, newName)
    haskellQvar.getNode.getTreeParent.replaceChild(haskellQvar.getNode, newHaskellVar.getNode)
    haskellQvar
  }


  def getName(haskellQvarop: HaskellQvarop): String = {
    haskellQvarop.getText
  }

  def getNameIdentifier(haskellQvarop: HaskellQvarop): PsiElement = {
    haskellQvarop
  }

  def setName(haskellQvarop: HaskellQvarop, newName: String): PsiElement = {
    val newHaskellQvarop = HaskellElementFactory.createQvarop(haskellQvarop.getProject, newName)
    haskellQvarop.getNode.getTreeParent.replaceChild(haskellQvarop.getNode, newHaskellQvarop.getNode)
    haskellQvarop
  }


  def getName(haskellQcon: HaskellQcon): String = {
    haskellQcon.getText
  }

  def getNameIdentifier(haskellQcon: HaskellQcon): PsiElement = {
    haskellQcon
  }

  def setName(haskellQCon: HaskellQcon, newName: String): PsiElement = {
    val newHaskellQcon = HaskellElementFactory.createQcon(haskellQCon.getProject, newName)
    haskellQCon.getNode.getTreeParent.replaceChild(haskellQCon.getNode, newHaskellQcon.getNode)
    haskellQCon
  }


  def getName(haskellQconop: HaskellQconop): String = {
    haskellQconop.getText
  }

  def getNameIdentifier(haskellQconop: HaskellQconop): PsiElement = {
    haskellQconop
  }

  def setName(haskellQconop: HaskellQconop, newName: String): PsiElement = {
    val newHaskellQconop = HaskellElementFactory.createQconop(haskellQconop.getProject, newName)
    haskellQconop.getNode.getTreeParent.replaceChild(newHaskellQconop.getNode, newHaskellQconop.getNode)
    haskellQconop
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
      PsiTreeUtil.findChildOfType(node, classOf[HaskellModuleDeclaration]).getModuleName
    }

    def getIcon(unused: Boolean): Icon = {
      import com.powertuple.intellij.haskell.HaskellIcons._
      haskellElement match {
        case _: HaskellTypeDeclaration => Type
        case _: HaskellDataDeclaration => Data
        case _: HaskellNewtypeDeclaration => NewType
        case _: HaskellClassDeclaration => Class
        case _: HaskellInstanceDeclaration => Instance
        case _: HaskellDefaultDeclaration => HaskellSmallLogo
        case _: HaskellTypeSignature => TypeSignature
        case _: HaskellForeignDeclaration => ForeignImport
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _ => HaskellSmallLogo
      }
    }
  }

  // Used in Navigate to Symbol
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
          case _ => HaskellIcons.HaskellSmallLogo
        }
      }
    }
  }

  // Used in Navigate to Declaration
  def getPresentation(declarationElement: HaskellDeclarationElement): ItemPresentation = {
    new HaskellItemPresentation(declarationElement) {

      def getPresentableText: String = {
        getDeclarationInfo(declarationElement)
      }
    }
  }

  private def getDeclarationInfo(declarationElement: HaskellDeclarationElement): String = {
    val removeAfter = (de: HaskellDeclarationElement, tokens: Seq[IElementType]) =>
      de.getNode.getChildren(null).takeWhile(e => !tokens.contains(e.getElementType)).map(_.getText).mkString.trim.replaceAll("\\s+", " ")

    val removeAfterComment = (de: HaskellDeclarationElement) => removeAfter(de, Seq(HaskellTypes.HS_COMMENT, HaskellTypes.HS_NCOMMENT))
    val removeAfterWhereOrEqual = (de: HaskellDeclarationElement) => removeAfter(de, Seq(HaskellTypes.HS_WHERE, HaskellTypes.HS_EQUAL))

    declarationElement match {
      case td: HaskellTypeDeclaration => removeAfterWhereOrEqual(td)
      case nt: HaskellNewtypeDeclaration => removeAfterWhereOrEqual(nt)
      case cd: HaskellClassDeclaration => removeAfterWhereOrEqual(cd)
      case id: HaskellInstanceDeclaration => removeAfterWhereOrEqual(id)
      case ts: HaskellTypeSignature => removeAfterWhereOrEqual(ts)
      case tf: HaskellTypeFamilyDeclaration => removeAfterWhereOrEqual(tf)
      case de => removeAfterComment(de)
    }
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getIdentifierElements.map(_.getName).mkString(" ")
  }

  def getIdentifierElements(typeSignature: HaskellTypeSignature): Seq[HaskellNamedElement] = {
    Option(typeSignature.getVars).map(_.getQvarList.toSeq).
        orElse(Option(typeSignature.getOps).map(_.getOpList.toSeq).map(hops => hops.flatMap(hop => Option(hop.getQconop).orElse(Option(hop.getQvarop))))).
        getOrElse(Seq())
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    dataDeclaration.getSimpletype.getIdentifierElements ++ Option(dataDeclaration.getConstr1List).map(_.map(_.getQcon)).toSeq.flatten ++
        Option(dataDeclaration.getConstr2List).map(_.map(p => p.getQconop)).toSeq.flatten ++ Option(dataDeclaration.getConstr3List).map(_.map(_.getQcon)).toSeq.flatten
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    newtypeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    Seq(classDeclaration.getQcon)
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    val inst = instanceDeclaration.getInst
    Seq(instanceDeclaration.getQcon) ++
        Option(inst.getQvar).map(Seq(_)).
            orElse(Option(inst.getGtycon).flatMap(g => Option(g.getQcon)).map(Seq(_))).
            orElse(Option(inst.getQconList).map(_.toSeq)).getOrElse(Seq())
  }

  def getIdentifierElements(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): Seq[HaskellNamedElement] = {
    val id = Option(PsiTreeUtil.findChildOfType(typeFamilyDeclaration.getTypeFamilyType, classOf[HaskellQcon])).
        orElse(Option(PsiTreeUtil.findChildOfType(typeFamilyDeclaration.getTypeFamilyType, classOf[HaskellQvarop])))
    id.toSeq
  }

  def getIdentifierElements(derivingDeclaration: HaskellDerivingDeclaration): Seq[HaskellNamedElement] = {
    Seq(derivingDeclaration.getQcon)
  }

  def getIdentifierElements(typeInstanceDeclaration: HaskellTypeInstanceDeclaration): Seq[HaskellNamedElement] = {
    val qcon = Option(PsiTreeUtil.findChildOfType(typeInstanceDeclaration, classOf[HaskellQcon]))
    qcon.toSeq
  }

  def getIdentifierElements(simpleType: HaskellSimpletype): Seq[HaskellNamedElement] = {
    Option(simpleType.getQcon).orElse(Option(simpleType.getQvarop)).orElse(Option(simpleType.getQconop)).toSeq
  }

  def getModuleName(importDeclaration: HaskellImportDeclaration): String = {
    importDeclaration.getImportModule.getQcon.getName
  }

  def getModuleName(moduleDeclaration: HaskellModuleDeclaration): String = {
    Option(moduleDeclaration.getQcon).map(_.getName).getOrElse("Undefined module")
  }
}