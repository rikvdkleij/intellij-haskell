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

import com.intellij.lang.ASTNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Computable
import com.intellij.psi.tree.{IElementType, TokenSet}
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellTypes._

import scala.collection.JavaConverters._

object HaskellPsiUtil {

  def findImportDeclarations(psiFile: PsiFile): Iterable[HaskellImportDeclaration] = {
    runReadAction { psiFile: PsiFile =>
      PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellImportDeclaration]).asScala
    }(psiFile)
  }

  def findLanguageExtensions(psiFile: PsiFile): Iterable[HaskellLanguagePragma] = {
    runReadAction { psiFile: PsiFile =>
      PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellLanguagePragma]).asScala
    }(psiFile)
  }

  def findNamedElement(psiElement: PsiElement): Option[HaskellNamedElement] = {
    runReadAction[PsiElement, Option[HaskellNamedElement]] {
      case e: HaskellNamedElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, NamedElementCondition)).map(_.asInstanceOf[HaskellNamedElement])
    }(psiElement)
  }

  def findModIdElement(psiElement: PsiElement): Option[HaskellModid] = {
    runReadAction[PsiElement, Option[HaskellModid]] {
      case e: HaskellModid => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ModIdElementCondition)).map(_.asInstanceOf[HaskellModid])
    }(psiElement)
  }

  def findNamedElements(psiElement: PsiElement): Iterable[HaskellNamedElement] = {
    runReadAction { psiElement: PsiElement =>
      PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellNamedElement]).asScala
    }(psiElement)
  }

  def findQualifiedNamedElements(psiElement: PsiElement): Iterable[HaskellQualifiedNameElement] = {
    runReadAction { psiElement: PsiElement =>
      PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellQualifiedNameElement]).asScala
    }(psiElement)
  }

  def findHaskellDeclarationElements(psiElement: PsiElement): Iterable[HaskellDeclarationElement] = {
    runReadAction { psiElement: PsiElement =>
      PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellDeclarationElement]).asScala.filter(e => e.getParent.getNode.getElementType == HS_TOP_DECLARATION || e.getNode.getElementType == HS_MODULE_DECLARATION)
    }(psiElement)
  }

  def findTopLevelDeclarations(psiElement: PsiElement): Iterable[HaskellDeclarationElement] = {
    runReadAction { psiElement: PsiElement =>
      findHaskellDeclarationElements(psiElement).filterNot(e => e.getNode.getElementType == HS_IMPORT_DECLARATION)
    }(psiElement)
  }

  def findModuleDeclaration(psiFile: PsiFile, runInRead: Boolean = false): Option[HaskellModuleDeclaration] = {
    runReadAction { psiFile: PsiFile =>
      Option(PsiTreeUtil.findChildOfType(psiFile.getOriginalFile, classOf[HaskellModuleDeclaration]))
    }(psiFile, runInRead)
  }

  def findModuleName(psiFile: PsiFile, runInRead: Boolean = false): Option[String] = {
    findModuleDeclaration(psiFile, runInRead).flatMap(_.getModuleName)
  }

  def findQualifiedNameElement(psiElement: PsiElement): Option[HaskellQualifiedNameElement] = {
    runReadAction[PsiElement, Option[HaskellQualifiedNameElement]] {
      case e: HaskellQualifiedNameElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, QualifiedNameElementCondition)).map(_.asInstanceOf[HaskellQualifiedNameElement])
    }(psiElement)
  }

  def findImportDeclarationsParent(psiElement: PsiElement): Option[HaskellImportDeclarations] = {
    runReadAction[PsiElement, Option[HaskellImportDeclarations]] {
      case e: HaskellImportDeclarations => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ImportDeclarationsCondition)).map(_.asInstanceOf[HaskellImportDeclarations])
    }(psiElement)
  }

  def findImportDeclarationParent(psiElement: PsiElement): Option[HaskellImportDeclaration] = {
    runReadAction[PsiElement, Option[HaskellImportDeclaration]] {
      case e: HaskellImportDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ImportDeclarationCondition)).map(_.asInstanceOf[HaskellImportDeclaration])
    }(psiElement)
  }

  def findImportHidingDeclarationParent(psiElement: PsiElement): Option[HaskellImportHidingSpec] = {
    runReadAction[PsiElement, Option[HaskellImportHidingSpec]] {
      case e: HaskellImportHidingSpec => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ImportHidingSpecCondition)).map(_.asInstanceOf[HaskellImportHidingSpec])
    }(psiElement)
  }

  def findTopDeclarationParent(psiElement: PsiElement): Option[HaskellTopDeclaration] = {
    runReadAction[PsiElement, Option[HaskellTopDeclaration]] {
      case e: HaskellTopDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, TopDeclarationElementCondition)).map(_.asInstanceOf[HaskellTopDeclaration])
    }(psiElement)
  }

  def findHighestDeclarationElementParent(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    runReadAction[PsiElement, Option[HaskellDeclarationElement]] {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, HighestDeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }(psiElement)
  }

  def findDeclarationElementParent(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    runReadAction[PsiElement, Option[HaskellDeclarationElement]] {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, DeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }(psiElement)
  }

  def findTopLevelTypeSignatures(psiElement: PsiElement): Iterable[HaskellTypeSignature] = {
    runReadAction { psiElement: PsiElement =>
      PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellTypeSignature]).asScala.filter(_.getParent.getNode.getElementType == HS_TOP_DECLARATION)
    }(psiElement)
  }

  def findModuleDeclarationParent(psiElement: PsiElement): Option[HaskellModuleDeclaration] = {
    runReadAction[PsiElement, Option[HaskellModuleDeclaration]] {
      case e: HaskellModuleDeclaration => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ModuleDeclarationCondition)).map(_.asInstanceOf[HaskellModuleDeclaration])
    }(psiElement)
  }

  def findExpressionParent(psiElement: PsiElement): Option[HaskellExpression] = {
    runReadAction[PsiElement, Option[HaskellExpression]] {
      case e: HaskellExpression => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, ExpressionCondition)).map(_.asInstanceOf[HaskellExpression])
    }(psiElement)
  }

  def getSelectionStartEnd(psiElement: PsiElement, editor: Editor): Option[(PsiElement, PsiElement)] = {
    runReadAction { psiElement: PsiElement =>
      val psiFile = psiElement.getContainingFile
      for {
        start <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionStart))
        end <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1))
      } yield (start, end)
    }(psiElement)
  }

  def getChildOfType[T <: PsiElement](psiElement: PsiElement, cls: Class[T]): Option[T] = {
    runReadAction { psiElement: PsiElement =>
      Option(PsiTreeUtil.getChildOfType(psiElement, cls))
    }(psiElement)
  }

  def getChildNodes(psiElement: PsiElement, typ: IElementType, typs: IElementType*): Array[ASTNode] = {
    runReadAction { psiElement: PsiElement =>
      psiElement.getNode.getChildren(TokenSet.create(typ +: typs: _*))
    }(psiElement)
  }

  def streamChildren[T <: PsiElement](psiElement: PsiElement, cls: Class[T]): Stream[T] = {
    runReadAction { psiElement: PsiElement =>
      PsiTreeUtil.childIterator(psiElement, cls).asScala.toStream
    }(psiElement)
  }

  /** Analogous to PsiTreeUtil.findFirstParent */
  def collectFirstParent[A](psiElement: PsiElement)(f: PartialFunction[PsiElement, A]): Option[A] = {
    runReadAction { psiElement: PsiElement =>
      Stream.iterate(psiElement.getParent)(_.getParent).takeWhile(_ != null).collectFirst(f)
    }(psiElement)
  }

  private def runReadAction[V, T](f: V => T)(x: V, runInRead: Boolean = false): T = {
    if (runInRead) {
      ApplicationManager.getApplication.runReadAction {
        new Computable[T] {
          override def compute(): T = {
            f(x)
          }
        }
      }
    }
    else {
      f(x)
    }
  }
}
