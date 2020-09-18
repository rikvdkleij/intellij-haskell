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
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.external.component.TypeInfoComponentResult.TypeInfo
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.psi._
import intellij.haskell.util.ScalaUtil

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

    case class CaseOf(psiElement: PsiElement) extends CaseSplitKind

  }

  def caseSplit(project: Project, psiElement: PsiElement, execute: Boolean = false): Boolean = {
    (for {
      caseSplitKind <- topLevelCaseSplitting(psiElement).orElse(caseOfCaseSplitting(psiElement))
      typeInfo <- HaskellComponentsManager.findTypeInfoForElement(psiElement).toOption
      currentDeclarationLine <- HaskellPsiUtil.findTopDeclarationLineParent(psiElement)
      constrs <- findDataConstructors(currentDeclarationLine, typeInfo).orElse(findNewtypeConstr(currentDeclarationLine, typeInfo))
    } yield {
      if (execute) {
        WriteCommandAction.runWriteCommandAction(project, ScalaUtil.runnable {
          caseSplitKind match {
            case CaseOf(element) => doCaseSplitCase(project, element, constrs)
            case tl: TopLevel => doTopLevelSplitCase(tl, project, constrs, currentDeclarationLine)
          }
        })
      }
    }).isDefined
  }

  private def caseOfCaseSplitting(psiElement: PsiElement): Option[CaseSplitKind] = {
    if (psiElement.getNode.getElementType == HaskellTypes.HS_UNDERSCORE) {
      Option(TreeUtil.findSiblingBackward(psiElement.getNode, HaskellTypes.HS_OF)).flatMap(x => Option(TreeUtil.findSibling(x, HaskellTypes.HS_NEWLINE))).map(_ => CaseOf(psiElement))
    } else {
      for {
        nameElement <- HaskellPsiUtil.findQName(psiElement)
        _ <- Option(PsiTreeUtil.findSiblingBackward(nameElement, HaskellTypes.HS_OF, null)).flatMap(x => Option(PsiTreeUtil.findSiblingForward(x, HaskellTypes.HS_NEWLINE, null)))
      } yield CaseOf(nameElement)
    }
  }

  private def topLevelCaseSplitting(psiElement: PsiElement): Option[CaseSplitKind] = {
    if (psiElement.getNode.getElementType == HaskellTypes.HS_UNDERSCORE) {
      Option(TreeUtil.findSibling(psiElement.getNode, HaskellTypes.HS_EQUAL)).map(_ => TopLevelUnderscore(psiElement))
    } else {
      HaskellPsiUtil.findQName(psiElement).flatMap(ne => Option(PsiTreeUtil.findSiblingForward(ne, HaskellTypes.HS_EQUAL, null)).map(_ => TopLevelNamed(ne.getIdentifierElement)))
    }
  }

  private def doTopLevelSplitCase(topLevel: TopLevel, project: Project, constrs: Seq[HaskellCompositeElement], currentDeclarationLine: HaskellTopDeclarationLine): Unit = {
    constrs.foreach { constr =>
      val newLine = currentDeclarationLine.getParent.addAfter(currentDeclarationLine.copy, currentDeclarationLine)
      val elementToReplace = topLevel match {
        case TopLevelUnderscore(psiElement) =>
          val firstChild = currentDeclarationLine.getFirstChild.getFirstChild
          val elementToReplaceIndex = firstChild.getNode.getChildren(null).indexWhere(_ == psiElement.getNode)
          newLine.getFirstChild.getFirstChild.getNode.getChildren(null).array.apply(elementToReplaceIndex).getPsi
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

  private def doCaseSplitCase(project: Project, psiElement: PsiElement, constrs: Seq[HaskellCompositeElement]): Unit = {
    for {
      expression <- HaskellPsiUtil.findExpression(psiElement)
      psi1 <- Option(TreeUtil.findSiblingBackward(psiElement.getNode, HaskellTypes.HS_NEWLINE)).map(_.getPsi)
      psi2 = Option(TreeUtil.findSibling(psiElement.getNode, HaskellTypes.HS_NEWLINE)).map(_.getPsi).getOrElse(expression.getLastChild)
      elements = PsiTreeUtil.getElementsOfRange(psi1, psi2)
    } yield {
      for (constr <- constrs) {
        val pattern = createPattern(project, constr, elements.asScala.flatMap(e => HaskellPsiUtil.findNamedElement(e)).toSeq)
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
      elements.asScala.foreach(e => if (e.isValid) e.delete())
    }
  }

  private def createPattern(project: Project, constr: HaskellCompositeElement, currentLineElements: Seq[HaskellNamedElement]): PsiElement = {
    val pattern = constr.copy()
    val currentNames = currentLineElements.map(_.getName)
    val patternNames = PatternNames.filterNot(currentNames.contains)
    HaskellPsiUtil.findNamedElements(pattern).tail.zip(patternNames).map { case (e, n) => e.replace(HaskellElementFactory.createVarid(project, n).get) }
    pattern
  }

  private def findDataConstructors(psiElement: HaskellTopDeclarationLine, typeInfo: TypeInfo): Option[Seq[HaskellConstr]] = {
    findDefinitionLocation(psiElement, typeInfo) match {
      case Some(e) =>
        HaskellPsiUtil.findNamedElement(e).flatMap(e => Option(e.getReference)).flatMap(r => Option(r.resolve)) match {
          case Some(e) => HaskellPsiUtil.findDataDeclaration(e).map(_.getConstrList.asScala.toSeq)
          case None => None
        }
      case None => None
    }
  }

  private def findDefinitionLocation(psiElement: HaskellTopDeclarationLine, typeInfo: TypeInfo): Option[HaskellNamedElement] = {
    val typeName = typeInfo.typeSignature.split("::").last.trim
    findTypeSignatureDeclaration(psiElement).flatMap(d => HaskellPsiUtil.findNamedElements(d).find(_.getName == typeName))
  }

  private def findTypeSignatureDeclaration(currentDeclarationLine: HaskellTopDeclarationLine): Option[HaskellTopDeclarationLine] = {
    Option(PsiTreeUtil.findSiblingBackward(currentDeclarationLine, HaskellTypes.HS_TOP_DECLARATION_LINE, null)) match {
      case Some(d: HaskellTopDeclarationLine) if Option(d.getFirstChild).flatMap(c => Option(c.getFirstChild)).exists(_.isInstanceOf[HaskellTypeSignature]) => Some(d)
      case Some(d: HaskellTopDeclarationLine) => findTypeSignatureDeclaration(d)
      case _ => None
    }
  }

  private def findNewtypeConstr(psiElement: HaskellTopDeclarationLine, typeInfo: TypeInfo): Option[Seq[HaskellNewconstr]] = {
    findDefinitionLocation(psiElement, typeInfo) match {
      case Some(e) =>
        HaskellPsiUtil.findNamedElement(e).flatMap(e => Option(e.getReference)).flatMap(r => Option(r.resolve)) match {
          case Some(e) => HaskellPsiUtil.findNewTypeDeclaration(e).map(x => Seq(x.getNewconstr))
          case None => None
        }
      case None => None
    }
  }
}