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

package intellij.haskell.psi

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellTypes.HS_TOP_DECLARATION

import scala.collection.JavaConversions._

object HaskellPsiUtil {

  def findImportDeclarations(psiFile: PsiFile): Iterable[HaskellImportDeclaration] = {
    PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellImportDeclaration])
  }

  def findLanguageExtensions(psiFile: PsiFile): Iterable[HaskellLanguagePragma] = {
    PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellLanguagePragma])
  }

  def findNamedElement(psiElement: PsiElement): Option[HaskellNamedElement] = {
    psiElement match {
      case e: HaskellNamedElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, NamedElementCondition)).map(_.asInstanceOf[HaskellNamedElement])
    }
  }

  def findModIdElement(psiElement: PsiElement): Option[HaskellModid] = {
    psiElement match {
      case e: HaskellModid => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ModIdElementCondition)).map(_.asInstanceOf[HaskellModid])
    }
  }

  def findNamedElements(psiElement: PsiElement): Iterable[HaskellNamedElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellNamedElement])
  }

  def findQualifiedNamedElements(psiElement: PsiElement): Iterable[HaskellQualifiedNameElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellQualifiedNameElement])
  }

  def findDeclarationElements(psiElement: PsiElement): Iterable[HaskellDeclarationElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellDeclarationElement]).filter(_.getParent.getNode.getElementType == HS_TOP_DECLARATION)
  }

  def findModuleDeclaration(psiFile: PsiFile): Option[HaskellModuleDeclaration] = {
    Option(PsiTreeUtil.findChildOfType(psiFile.getOriginalFile, classOf[HaskellModuleDeclaration]))
  }

  def findModuleName(psiFile: PsiFile): Option[String] = {
    findModuleDeclaration(psiFile).flatMap(_.getModuleName)
  }

  def findQualifiedNameElement(psiElement: PsiElement): Option[HaskellQualifiedNameElement] = {
    psiElement match {
      case e: HaskellQualifiedNameElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, QualifiedNameElementCondition)).map(_.asInstanceOf[HaskellQualifiedNameElement])
    }
  }

  def findImportDeclarationParent(psiElement: PsiElement): Option[HaskellImportDeclaration] = {
    psiElement match {
      case e: HaskellImportDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ImportDeclarationCondition)).map(_.asInstanceOf[HaskellImportDeclaration])
    }
  }

  def findTopDeclarationParent(psiElement: PsiElement): Option[HaskellTopDeclaration] = {
    psiElement match {
      case e: HaskellTopDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, TopDeclarationElementCondition)).map(_.asInstanceOf[HaskellTopDeclaration])
    }
  }

  def findDeclarationElementParent(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    psiElement match {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, DeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }
  }

  def findTopLevelTypeSignatures(element: PsiElement): Iterable[HaskellTypeSignature] = {
    PsiTreeUtil.findChildrenOfType(element, classOf[HaskellTypeSignature]).filter(_.getParent.getNode.getElementType == HS_TOP_DECLARATION)
  }

  def findModuleDeclarationParent(psiElement: PsiElement): Option[HaskellModuleDeclaration] = {
    psiElement match {
      case e: HaskellModuleDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ModuleDeclarationCondition)).map(_.asInstanceOf[HaskellModuleDeclaration])
    }
  }
}
