/*
 * Copyright 2014-2018 Rik van der Kleij
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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.{IElementType, TokenSet}
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util.ApplicationUtil

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object HaskellPsiUtil {

  def findImportDeclarations(psiFile: PsiFile): Iterable[HaskellImportDeclaration] = {
    PsiTreeUtil.findChildrenOfType(psiFile.getOriginalFile, classOf[HaskellImportDeclaration]).asScala
  }

  def findLanguageExtensions(psiFile: PsiFile): Iterable[HaskellPragma] = {
    Option(PsiTreeUtil.findChildOfType(psiFile, classOf[HaskellFileHeader])) match {
      case Some(e) => PsiTreeUtil.findChildrenOfType(e, classOf[HaskellPragma]).asScala
      case None => Iterable()
    }
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
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_MODID)).map(_.getPsi.asInstanceOf[HaskellModid])
    }
  }

  def findDataConstr(psiElement: PsiElement): Option[HaskellConstr] = {
    psiElement match {
      case e: HaskellConstr => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_CONSTR)).map(_.getPsi.asInstanceOf[HaskellConstr])
    }
  }

  def findDataFieldDecl(psiElement: PsiElement): Option[HaskellFielddecl] = {
    psiElement match {
      case e: HaskellFielddecl => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_FIELDDECL)).map(_.getPsi.asInstanceOf[HaskellFielddecl])
    }
  }

  def findNamedElements(psiElement: PsiElement): Iterable[HaskellNamedElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellNamedElement]).asScala
  }

  def findQualifiedNamedElements(psiElement: PsiElement): Iterable[HaskellQualifiedNameElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellQualifiedNameElement]).asScala
  }

  def findHaskellDeclarationElements(psiElement: PsiElement): Iterable[HaskellDeclarationElement] = {
    PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellDeclarationElement]).asScala.
      filter(e => e.getParent.getNode.getElementType == HS_TOP_DECLARATION || e.getNode.getElementType == HS_MODULE_DECLARATION)
  }

  def findTopLevelDeclarations(psiFile: PsiFile): Iterable[HaskellDeclarationElement] = {
    findHaskellDeclarationElements(psiFile).filterNot(e => Seq(HS_IMPORT_DECLARATION, HS_MODULE_DECLARATION).contains(e.getNode.getElementType))
  }

  def findModuleDeclaration(psiFile: PsiFile): Option[HaskellModuleDeclaration] = {
    Option(PsiTreeUtil.findChildOfType(psiFile.getOriginalFile, classOf[HaskellModuleDeclaration]))
  }

  def findModuleNameInPsiTree(psiFile: PsiFile): Option[String] = {
    Option(PsiTreeUtil.findChildOfType(psiFile.getOriginalFile, classOf[HaskellModuleDeclaration])).flatMap(_.getModuleName)
  }

  private final val ModuleNameCache: LoadingCache[PsiFile, Option[String]] = Scaffeine().build((psiFile: PsiFile) => {
    ApplicationUtil.runReadAction(findModuleNameInPsiTree(psiFile))
  })

  def findModuleName(psiFile: PsiFile): Option[String] = {
    ModuleNameCache.get(psiFile.getOriginalFile) match {
      case mn@Some(_) => mn
      case None =>
        ModuleNameCache.invalidate(psiFile)
        None
    }
  }

  def invalidateModuleName(psiFile: PsiFile): Unit = {
    ModuleNameCache.invalidate(psiFile)
  }

  def invalidateAllModuleNames(project: Project): Unit = {
    ModuleNameCache.asMap().keys.filter(_.getProject == project).foreach(ModuleNameCache.invalidate)
  }

  def findTopDeclarationLineParent(psiElement: PsiElement): Option[HaskellTopDeclarationLine] = {
    psiElement match {
      case e: HaskellTopDeclarationLine => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_TOP_DECLARATION_LINE)).map(_.getPsi.asInstanceOf[HaskellTopDeclarationLine])
    }
  }

  def findQualifiedName(psiElement: PsiElement): Option[HaskellQualifiedNameElement] = {
    psiElement match {
      case e: HaskellQualifiedNameElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, QualifiedNameElementCondition)).map(_.asInstanceOf[HaskellQualifiedNameElement])
    }
  }

  def findTtype(psiElement: PsiElement): Option[HaskellTtype] = {
    psiElement match {
      case e: HaskellTtype => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_TTYPE)).map(_.getPsi.asInstanceOf[HaskellTtype])
    }
  }

  def findImportDeclarations(psiElement: PsiElement): Option[HaskellImportDeclarations] = {
    psiElement match {
      case e: HaskellImportDeclarations => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_IMPORT_DECLARATIONS)).map(_.getPsi.asInstanceOf[HaskellImportDeclarations])
    }
  }

  def findImportDeclaration(psiElement: PsiElement): Option[HaskellImportDeclaration] = {
    psiElement match {
      case e: HaskellImportDeclaration => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_IMPORT_DECLARATION)).map(_.getPsi.asInstanceOf[HaskellImportDeclaration])
    }
  }

  def findHighestDeclarationElement(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    psiElement match {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, HighestDeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }
  }

  def findDeclarationElement(psiElement: PsiElement): Option[HaskellDeclarationElement] = {
    psiElement match {
      case e: HaskellDeclarationElement => Some(e)
      case e => Option(PsiTreeUtil.findFirstParent(e, DeclarationElementCondition)).map(_.asInstanceOf[HaskellDeclarationElement])
    }
  }

  def findTopLevelExpressions(psiFile: PsiFile): Iterable[HaskellExpression] = {
    PsiTreeUtil.findChildrenOfType(psiFile, classOf[HaskellExpression]).asScala
  }

  def findTypeSignatureDeclaration(psiElement: PsiElement): Option[HaskellTypeSignature] = {
    psiElement match {
      case e: HaskellTypeSignature => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_TYPE_SIGNATURE)).map(_.getPsi.asInstanceOf[HaskellTypeSignature])
    }
  }

  def findExpression(psiElement: PsiElement): Option[HaskellExpression] = {
    psiElement match {
      case e: HaskellExpression => Some(e)
      case e => Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_EXPRESSION)).map(_.getPsi.asInstanceOf[HaskellExpression])
    }
  }

  def getSelectionStartEnd(psiElement: PsiElement, editor: Editor): Option[(PsiElement, PsiElement)] = {
    val psiFile = psiElement.getContainingFile.getOriginalFile
    if (Option(editor.getSelectionModel.getSelectedText).isDefined) {
      for {
        start <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionStart))
        end <- Option(psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - (if (Option(editor.getSelectionModel.getSelectedText).exists(_.length > 1)) 1 else 0)))
      } yield (start, end)
    } else {
      None
    }
  }

  @tailrec
  def untilNonWhitespaceBackwards(element: Option[PsiElement]): Option[PsiElement] = {
    element match {
      case Some(e) if e.getNode.getElementType == HaskellTypes.HS_NEWLINE || e.getNode.getElementType == TokenType.WHITE_SPACE =>
        untilNonWhitespaceBackwards(Option(e.getPrevSibling))
      case e => e
    }
  }

  def getChildOfType[T <: PsiElement](psiElement: PsiElement, cls: Class[T]): Option[T] = {
    Option(PsiTreeUtil.getChildOfType(psiElement, cls))
  }

  def getChildNodes(psiElement: PsiElement, elementTypes: IElementType*): Array[ASTNode] = {
    psiElement.getNode.getChildren(TokenSet.create(elementTypes: _*))
  }

  def streamChildren[T <: PsiElement](psiElement: PsiElement, cls: Class[T]): Iterable[T] = {
    PsiTreeUtil.collectElementsOfType(psiElement, cls).asScala
  }

  /** Analogous to PsiTreeUtil.findFirstParent */
  def collectFirstParent[A](psiElement: PsiElement)(f: PartialFunction[PsiElement, A]): Option[A] = {
    Stream.iterate(psiElement.getParent)(_.getParent).takeWhile(_ != null).collectFirst(f)
  }

}
