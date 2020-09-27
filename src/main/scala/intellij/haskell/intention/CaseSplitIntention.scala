/*
 * Copyright 2014-2020 Rik van der Kleij
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

package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.FileContentUtilCore
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.psi._
import intellij.haskell.util.ScalaUtil

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

class CaseSplitIntention extends PsiElementBaseIntentionAction {

  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    CaseSplitIntention.caseSplit(project, psiElement, execute = true)
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    if (!StackProjectManager.isInitializing(project)) {
      CaseSplitIntention.caseSplit(project, psiElement)
    } else {
      false
    }
  }

  override def getFamilyName: String = "Case Split"

  override def getText: String = "Case split"

  override def startInWriteAction(): Boolean = false
}


object CaseSplitIntention {

  import intellij.haskell.intention.CaseSplitIntention.CaseSplitKind._

  private val PatternNames = Seq("n", "m", "i", "j", "k", "l", "s")

  private sealed trait CaseSplitKind

  private object CaseSplitKind {

    sealed trait TopLevel extends CaseSplitKind

    case class TopLevelUnderscore(psiElement: PsiElement) extends TopLevel

    case class TopLevelNamed(namedElement: HaskellNamedElement) extends TopLevel

    case class SubTopLevel(psiElement: PsiElement) extends CaseSplitKind

  }

  def caseSplit(project: Project, psiElement: PsiElement, execute: Boolean = false): Boolean = {
    (for {
      caseSplitKind <- subTopLevelSplitting(psiElement).orElse(topLevelCaseSplitting(psiElement))
      typeInfo <- HaskellComponentsManager.findTypeInfoForElement(psiElement).toOption
      currentDeclarationLine <- HaskellPsiUtil.findTopDeclarationLineParent(psiElement)
      typeName = typeInfo.typeSignature.split("::").last.trim
      constrs <- findDataConstructors(currentDeclarationLine, typeName).orElse(findNewtypeConstr(currentDeclarationLine, typeName))
    } yield {
      if (execute) {
        val vFile = psiElement.getContainingFile.getVirtualFile
        WriteCommandAction.runWriteCommandAction(project, ScalaUtil.runnable {
          caseSplitKind match {
            case SubTopLevel(element) => doSubTopLevelSpitCase(project, element, constrs)
            case tl: TopLevel => doTopLevelSplitCase(tl, project, constrs, currentDeclarationLine)
          }
        })
        FileContentUtilCore.reparseFiles(vFile)
      }
    }).isDefined
  }

  private def subTopLevelSplitting(psiElement: PsiElement): Option[CaseSplitKind] = {
    if (psiElement.getNode.getElementType == HaskellTypes.HS_UNDERSCORE) {
      Option(TreeUtil.findSiblingBackward(psiElement.getNode, TokenSet.create(HaskellTypes.HS_WHERE, HaskellTypes.HS_OF))).flatMap(x => Option(TreeUtil.findSibling(x, HaskellTypes.HS_NEWLINE))).map(_ => SubTopLevel(psiElement))
    } else {
      for {
        nameElement <- HaskellPsiUtil.findQName(psiElement)
        _ <- Option(TreeUtil.findSiblingBackward(nameElement.getNode, TokenSet.create(HaskellTypes.HS_WHERE, HaskellTypes.HS_OF))).flatMap(x => Option(PsiTreeUtil.findSiblingForward(x.getPsi, HaskellTypes.HS_NEWLINE, null)))
      } yield SubTopLevel(nameElement)
    }
  }

  private def topLevelCaseSplitting(psiElement: PsiElement): Option[CaseSplitKind] = {
    if (psiElement.getNode.getElementType == HaskellTypes.HS_UNDERSCORE) {
      Option(TreeUtil.findSibling(psiElement.getNode, HaskellTypes.HS_EQUAL)).map(_ => TopLevelUnderscore(psiElement))
    } else {
      HaskellPsiUtil.findQName(psiElement).flatMap(ne => Option(PsiTreeUtil.findSiblingForward(ne, HaskellTypes.HS_EQUAL, null)).map(_ => TopLevelNamed(ne.getIdentifierElement)))
    }
  }

  private def doTopLevelSplitCase(topLevel: TopLevel, project: Project, constrs: Seq[HaskellCompositeElement], currentDeclarationLine: HaskellTopDeclaration): Unit = {
    constrs.foreach { constr =>
      val newLine = if (currentDeclarationLine.getNode.getChildren(TokenSet.create(HaskellTypes.HS_NEWLINE)).nonEmpty) {
        currentDeclarationLine.getParent.addAfter(currentDeclarationLine.copy, currentDeclarationLine)
      } else {
        val nl = HaskellElementFactory.createNewLine(project)
        val newline = currentDeclarationLine.copy()
        currentDeclarationLine.getParent.addAfter(newline, currentDeclarationLine)
      }
      val elementToReplace = topLevel match {
        case TopLevelUnderscore(psiElement) =>
          val firstChild = currentDeclarationLine.getFirstChild
          val elementToReplaceIndex = firstChild.getNode.getChildren(null).indexWhere(_ == psiElement.getNode)
          newLine.getFirstChild.getNode.getChildren(null).array.apply(elementToReplaceIndex).getPsi
        case TopLevelNamed(psiElement) =>
          val elementToReplaceIndex = HaskellPsiUtil.findNamedElements(currentDeclarationLine).toArray.indexWhere(_ == psiElement)
          HaskellPsiUtil.findNamedElements(newLine).toArray.apply(elementToReplaceIndex)
      }

      val pattern = createPattern(project, constr, HaskellPsiUtil.findNamedElements(currentDeclarationLine).toSeq)
      val newElement = elementToReplace.replace(pattern)

      if (HaskellPsiUtil.findNamedElements(constr).size > 1) {
        AddParensIntention.addParens(project, newElement)
      }
    }
    currentDeclarationLine.delete()
  }

  private def doSubTopLevelSpitCase(project: Project, psiElement: PsiElement, constrs: Seq[HaskellCompositeElement]): Unit = {
    for {
      expression <- HaskellPsiUtil.findExpression(psiElement)
      psi1 <- Option(TreeUtil.findSiblingBackward(psiElement.getNode, HaskellTypes.HS_NEWLINE)).map(_.getPsi)
      psi2 = Option(TreeUtil.findSibling(psiElement.getNode, HaskellTypes.HS_NEWLINE)).flatMap(e => Option(e.getPsi.getPrevSibling)).getOrElse(expression.getLastChild)
      elements = PsiTreeUtil.getElementsOfRange(psi1, psi2)
    } yield {
      for (constr <- constrs) {
        val pattern = createPattern(project, constr, elements.asScala.flatMap(e => HaskellPsiUtil.findNamedElements(e)).toSeq)
        elements.forEach(e =>
          if (e == psiElement) {
            val newElement = expression.add(pattern)
            if (HaskellPsiUtil.findNamedElements(constr).size > 1) {
              AddParensIntention.addParens(project, newElement)
            }
          } else {
            expression.add(e)
          }
        )
      }
      expression.deleteChildRange(psi1, psi2)
    }
  }

  private def createPattern(project: Project, constr: HaskellCompositeElement, currentLineElements: Seq[HaskellNamedElement]): PsiElement = {
    def defaultPatternNamedElements(elements: Seq[HaskellQName]) = {
      if (elements.map(_.getIdentifierElement).exists(_.isInstanceOf[HaskellConsym])) {
        elements.filterNot(x => x.getIdentifierElement.isInstanceOf[HaskellConsym])
      } else {
        if (elements.nonEmpty) {
          elements.tail
        } else {
          elements
        }
      }
    }

    val pattern = constr.copy()
    val currentNames = currentLineElements.map(_.getName)
    val patternNames = PatternNames.filterNot(currentNames.contains)

    val namedElements = pattern match {
      case c: HaskellConstr => Option(c.getFirstChild) match {
        case Some(constr1: HaskellConstr1) =>
          val fieldDecls = constr1.getFielddeclList.asScala
          fieldDecls.map(_.getTtype).foreach(_.delete())
          fieldDecls.flatMap(_.getNode.getChildren(TokenSet.create(HaskellTypes.HS_COLON_COLON))).foreach(_.getPsi.delete())
          constr1.getNode.getChildren(TokenSet.create(HaskellTypes.HS_COMMA, HaskellTypes.HS_LEFT_BRACE, HaskellTypes.HS_RIGHT_BRACE)).foreach(_.getPsi.delete())
          fieldDecls.flatMap(_.getQNameList.asScala)
        case Some(constr2: HaskellConstr2) =>
          val firstTtype = constr2.getTtypeList.asScala.headOption
          val qNameElements = firstTtype.toSeq.flatMap(_.getQNameList.asScala)
          val typeElements = firstTtype.toSeq.flatMap(_.getTtypeList.asScala.headOption)

          defaultPatternNamedElements(qNameElements) ++ typeElements
        case _ =>
          val elements = HaskellPsiUtil.findQNameElements(pattern).toSeq
          defaultPatternNamedElements(elements)
      }
      case _ =>
        val elements = HaskellPsiUtil.findQNameElements(pattern).toSeq
        defaultPatternNamedElements(elements)
    }

    if (namedElements.isEmpty) {
      // Do nothing
    } else {
      namedElements.zip(patternNames).map { case (e, n) =>
        HaskellPsiUtil.findTtype(e).find(e => Option(e.getNextSibling).map(_.getNode).exists(e => List(HaskellTypes.HS_RIGHT_BRACKET, HaskellTypes.HS_RIGHT_PAREN).contains(e.getElementType))) match {
          case Some(ttype) =>
            ttype.getNextSibling.delete()
            ttype.getPrevSibling.delete()
          case None => ()
        }
        e.replace(HaskellElementFactory.createQNameElement(project, n).get)
      }
    }
    pattern
  }

  private def findDataConstructors(psiElement: HaskellTopDeclaration, typeName: String): Option[Seq[HaskellConstr]] = {
    typeName match {
      case s"[$x]" =>
        val d = HaskellElementFactory.createDataDeclaration(psiElement.getProject, "data [] a = [] | a : a") // Just a hack to create the right pattern in createPattern
        d.map(_.getConstrList.asScala.toSeq)
      case _ =>
        findDefinitionLocation(psiElement, typeName) match {
          case Some(e) =>
            HaskellPsiUtil.findNamedElement(e).flatMap(e => Option(e.getReference)).flatMap(r => Option(r.resolve)) match {
              case Some(e) => HaskellPsiUtil.findDataDeclaration(e).map(_.getConstrList.asScala.toSeq)
              case None => None
            }
          case None => None
        }
    }
  }

  private def findDefinitionLocation(psiElement: HaskellTopDeclaration, typeName: String): Option[HaskellNamedElement] = {
    findTypeSignatureDeclaration(psiElement).flatMap(d => HaskellPsiUtil.findNamedElements(d).find(_.getName == typeName))
  }

  @tailrec
  private def findTypeSignatureDeclaration(currentDeclarationLine: HaskellTopDeclaration): Option[HaskellTopDeclaration] = {
    Option(PsiTreeUtil.findSiblingBackward(currentDeclarationLine, HaskellTypes.HS_TOP_DECLARATION, null)) match {
      case Some(d: HaskellTopDeclaration) if Option(d.getFirstChild).exists(_.isInstanceOf[HaskellTypeSignature]) => Some(d)
      case Some(d: HaskellTopDeclaration) => findTypeSignatureDeclaration(d)
      case _ => None
    }
  }

  private def findNewtypeConstr(psiElement: HaskellTopDeclaration, typeName: String): Option[Seq[HaskellNewconstr]] = {
    findDefinitionLocation(psiElement, typeName) match {
      case Some(e) =>
        HaskellPsiUtil.findNamedElement(e).flatMap(e => Option(e.getReference)).flatMap(r => Option(r.resolve)) match {
          case Some(e) => HaskellPsiUtil.findNewTypeDeclaration(e).map(x => Seq(x.getNewconstr))
          case None => None
        }
      case None => None
    }
  }
}