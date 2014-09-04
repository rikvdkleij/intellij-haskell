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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.util.ArrayUtil
import com.powertuple.intellij.haskell.HaskellIcons
import com.powertuple.intellij.haskell.psi._
import org.jetbrains.annotations.Nullable

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

  // Used in Navigate to Symbol
  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {
    new ItemPresentation {

      @Nullable
      def getPresentableText: String = {
        val declarationElement = Option(PsiTreeUtil.findFirstParent(namedElement, declarationElementCondition))
        declarationElement match {
          case Some(de: HaskellDeclarationElement) => getDeclarationInfo(de)
          case _ => namedElement.getName
        }
      }

      @Nullable
      def getLocationString: String = {
        namedElement.getContainingFile.getName
      }

      @Nullable
      def getIcon(unused: Boolean): Icon = {
        HaskellIcons.HaskellSmallLogo
      }
    }
  }

  // Used in Navigate to Declaration
  def getPresentation(declarationElement: HaskellDeclarationElement): ItemPresentation = {
    new ItemPresentation {

      @Nullable
      def getPresentableText: String = {
        getDeclarationInfo(declarationElement)
      }

      @Nullable
      def getLocationString: String = {
        val node = declarationElement.getContainingFile.getNode.getPsi
        val m = PsiTreeUtil.findChildOfType(node, classOf[HaskellModuleDeclaration]).getModuleName
        m
      }

      @Nullable
      def getIcon(unused: Boolean): Icon = {
        HaskellIcons.HaskellSmallLogo
      }
    }
  }

  private def getDeclarationInfo(declarationElement: HaskellDeclarationElement): String = {
    val removeAfter: (String => String => String) = a => s => s.split(a)(0).trim
    val removeComment = removeAfter("--")
    val removeAfterWhere = removeAfter(" where")
    val removeAfterEqual = removeAfter(" =")

    def shortenIfTooLong(declarationElement: String, shortenWith: String => String): String = {
      val info = declarationElement.split("--")(0).trim
      if (info.length < 26) info else {
        shortenWith(info)
      }
    }
    declarationElement match {
      //      case ts: HaskellTypeSignature => removeComment(ts.getText)
      //      case dd: HaskellDataDeclaration => dd.getText.split(" = ")(0)
      //      case td: HaskellTypeDeclaration => td.getText
      //      case nt: HaskellNewtypeDeclaration => nt.getText
      //      case cd: HaskellClassDeclaration => cd.getText
            case id: HaskellInstanceDeclaration => shortenIfTooLong(id.getText, removeAfterWhere)

      //      case tf: HaskellTypeFamilyDeclaration => tf.getText
      //      case dd: HaskellDerivingDeclaration => dd.getText
      case de => removeComment(de.getText)
    }
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getIdentifierElement.getName
  }

  /**
   * Only returns first var. Could be a number of vars with same type signature.
   * TODO: fix
   */
  def getIdentifierElement(typeSignature: HaskellTypeSignature): HaskellNamedElement = {
    Option(typeSignature.getVars).map(_.getQvarList.head).orElse(Option(typeSignature.getOps.getOpList.head).flatMap(op => Option(op.getQconop).orElse(Option(op.getQvarop)))).orNull
  }

  def getIdentifierElement(dataDeclaration: HaskellDataDeclaration): HaskellNamedElement = {
    dataDeclaration.getSimpletype.getIdentifierElement
  }

  def getIdentifierElement(typeDeclaration: HaskellTypeDeclaration): HaskellNamedElement = {
    typeDeclaration.getSimpletype.getIdentifierElement
  }

  def getIdentifierElement(newtypeDeclaration: HaskellNewtypeDeclaration): HaskellNamedElement = {
    newtypeDeclaration.getSimpletype.getIdentifierElement
  }

  def getIdentifierElement(classDeclaration: HaskellClassDeclaration): HaskellNamedElement = {
    classDeclaration.getQcon
  }

  def getIdentifierElement(instanceDeclaration: HaskellInstanceDeclaration): HaskellNamedElement = {
    instanceDeclaration.getQcon
  }

  def getIdentifierElement(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): HaskellNamedElement = {
    typeFamilyDeclaration.getQcon
  }

  def getIdentifierElement(derivingDeclaration: HaskellDerivingDeclaration): HaskellNamedElement = {
    derivingDeclaration.getQcon
  }

  def getIdentifierElement(simpleType: HaskellSimpletype): HaskellNamedElement = {
    simpleType.getQcon
  }

  def getIdentifierElement(constr: HaskellConstr): HaskellNamedElement = {
    Option(constr.getConstr1).map(_.getQcon).
        orElse(Option(constr.getConstr2).map(_.getQconop)).
        orElse(Option(constr.getConstr3).map(_.getQcon)).orNull
  }

  def getModuleName(importDeclaration: HaskellImportDeclaration): String = {
    importDeclaration.getImportModule.getQcon.getName
  }

  def getModuleName(moduleDeclaration: HaskellModuleDeclaration): String = {
    Option(moduleDeclaration.getQcon).map(_.getName).getOrElse("Undefined module")
  }
}