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

import com.intellij.openapi.editor.Editor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellTypes._

import scala.collection.JavaConverters._

object HaskellPsiUtil {

  def findImportDeclarations(psiFile: PsiFile): Iterable[HaskellImportDeclaration] = {
    PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellImportDeclaration]).asScala
  }

  def findLanguageExtensions(psiFile: PsiFile): Iterable[HaskellLanguagePragma] = {
    PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellLanguagePragma]).asScala
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
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellNamedElement]).asScala
  }

  def findQualifiedNamedElements(psiElement: PsiElement): Iterable[HaskellQualifiedNameElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellQualifiedNameElement]).asScala
  }

  def findDeclarationElements(psiElement: PsiElement): Iterable[HaskellDeclarationElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellDeclarationElement]).asScala.filter(e => e.getParent.getNode.getElementType == HS_TOP_DECLARATION || e.getNode.getElementType == HS_MODULE_DECLARATION)
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

  def findImportDeclarationsParent(psiElement: PsiElement): Option[HaskellImportDeclarations] = {
    psiElement match {
      case e: HaskellImportDeclarations => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ImportDeclarationsCondition)).map(_.asInstanceOf[HaskellImportDeclarations])
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

  def findHighestDeclarationElementParent(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    psiElement match {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, HighestDeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }
  }

  def findDeclarationElementParent(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    psiElement match {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, DeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }
  }

  def findTopLevelTypeSignatures(element: PsiElement): Iterable[HaskellTypeSignature] = {
    PsiTreeUtil.findChildrenOfType(element, classOf[HaskellTypeSignature]).asScala.filter(_.getParent.getNode.getElementType == HS_TOP_DECLARATION)
  }

  def findModuleDeclarationParent(psiElement: PsiElement): Option[HaskellModuleDeclaration] = {
    psiElement match {
      case e: HaskellModuleDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ModuleDeclarationCondition)).map(_.asInstanceOf[HaskellModuleDeclaration])
    }
  }

  def findExpressionParent(psiElement: PsiElement): Option[HaskellExpression] = {
    psiElement match {
      case e: HaskellExpression => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ExpressionCondition)).map(_.asInstanceOf[HaskellExpression])
    }
  }

  def getSelectionStartEnd(psiElement: PsiElement, editor: Editor): Option[(PsiElement, PsiElement)] = {
    val psiFile = psiElement.getContainingFile
    for {
      start <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionStart))
      end <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1))
    } yield (start, end)
  }
}
