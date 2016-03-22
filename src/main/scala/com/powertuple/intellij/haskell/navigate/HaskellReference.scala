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

package com.powertuple.intellij.haskell.navigate

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.navigate.HaskellReference._
import com.powertuple.intellij.haskell.psi.HaskellTypes._
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.HaskellElementCondition._
import com.powertuple.intellij.haskell.util.{FileUtil, HaskellEditorUtil, HaskellElementCondition, LineColumnPosition}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  private val file = myElement.getContainingFile
  private lazy val typeInfo = GhcModTypeInfo.findTypeInfoFor(file, myElement)

  private class BuiltInResolveResult(val typeSignature: String, val libraryName: String, val module: String) extends ResolveResult {
    override def getElement: PsiElement = null

    override def isValidResult: Boolean = false
  }

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = file.getProject
    val result = myElement match {
      case me: HaskellModId =>
        (for {
          filePath <- FileUtil.findModuleFilePath(me.getText, project)
          file <- findFile(filePath)
        } yield new HaskellFileResolveResult(file.getOriginalElement)).toArray
      case qe: HaskellQualifier =>
        val importDeclaration = PsiTreeUtil.findChildrenOfType(file, classOf[HaskellImportDeclaration]).find(imd => Option(imd.getImportQualifiedAs).exists(_.getQualifier.getName == qe.getName))
        importDeclaration.map(e => Array(new HaskellGlobalResolveResult(e.getImportQualifiedAs.getQualifier))).getOrElse(Array())
      case ne: HaskellNamedElement =>
        resolveResults(project, ne.getName).toArray
      case _ => Array()
    }
    result.asInstanceOf[Array[ResolveResult]]
  }

  override def isReferenceTo(element: PsiElement): Boolean = {
    val psiManager = getElement.getManager
    val resolveResults = multiResolve(false)
    resolveResults.exists(rr => psiManager.areElementsEquivalent(rr.getElement, element)) ||
        resolveResults.exists(rr => Option(rr.getElement).exists(_.getText == element.getText))
  }

  override def getVariants: Array[AnyRef] = {
    if (element.getNode.getElementType != HS_VAR_DOT_SYM) {
      val localLookupElements = findLocalNamedElements.flatMap(createLookupElements)
      val declarationLookupElements = findDeclarationElementsInFile(file).filterNot(_.getIdentifierElements.contains(myElement)).flatMap(createLookupElements)
      (localLookupElements ++ declarationLookupElements).toArray
    } else {
      Array()
    }
  }

  private def resolveResults(project: Project, identifier: String) = {
    val globalResolveResults = getIdentifierInfos(file, myElement).flatMap {
      case pii: ProjectIdentifierInfo => resolveProjectReference(pii, identifier).map(new HaskellGlobalResolveResult(_)).toIterable
      case lii: LibraryIdentifierInfo => lii.filePath.flatMap(findFile).map(resolveDeclarationReferencesInFile(_, identifier).map(new HaskellGlobalResolveResult(_))).getOrElse(Iterable())
      case bii: BuiltInIdentifierInfo => Iterable(createBuiltInResolveResult(bii, file))
    }

    displayLabelInfoMessageIfResultsContainsBuiltInDefinition(globalResolveResults, project)

    if (globalResolveResults.isEmpty) {
      findLocalLhsElements(identifier).map(e => new HaskellLocalResolveResult(e)).toIterable
    } else {
      globalResolveResults
    }
  }

  private def displayLabelInfoMessageIfResultsContainsBuiltInDefinition(resolveResultsByGhcMod: Iterable[ResolveResult], project: Project) {
    val firstBuiltInResolveResult = resolveResultsByGhcMod.find(_.isInstanceOf[BuiltInResolveResult])
    firstBuiltInResolveResult match {
      case Some(birs: BuiltInResolveResult) => HaskellEditorUtil.createLabelMessage(s"${StringUtil.unescapeXml(birs.typeSignature)} is built-in: ${birs.libraryName}:${birs.module}", project)
      case _ => ()
    }
  }

  private def createLookupElements(compositeElement: HaskellCompositeElement): Seq[LookupElementBuilder] = {
    compositeElement match {
      case ne: HaskellNamedElement if ne != null => Seq(LookupElementBuilder.create(ne.getName).withTypeText(GhcModTypeInfo.findTypeInfoFor(ne.getContainingFile, ne).map(ti => StringUtil.unescapeXml(ti.typeSignature)).getOrElse("")).withIcon(HaskellIcons.HaskellSmallBlueLogo))
      case de: HaskellDeclarationElement => for {
        ne <- de.getIdentifierElements.toSeq
        leb <- Option(ne) match {
          case Some(n) => Seq(LookupElementBuilder.create(n.getName).withTypeText(getTypeText(de)).withIcon(de.getPresentation.getIcon(false)))
          case None => Seq()
        }
      } yield leb
    }
  }

  private def getTypeText(declarationElement: HaskellDeclarationElement) = {
    val presentableText = declarationElement.getPresentation.getPresentableText
    val typeSignatureDoubleColonIndex = presentableText.indexOf("::")
    if (typeSignatureDoubleColonIndex > 0) {
      presentableText.drop(typeSignatureDoubleColonIndex + 2).trim
    } else {
      presentableText
    }
  }

  private implicit object ElementOrdering extends Ordering[HaskellNamedElement] {
    override def compare(x: HaskellNamedElement, y: HaskellNamedElement): Int = {
      x.getTextOffset.compare(y.getTextOffset)
    }
  }

  private def findNamedElementFromDeclaration(psiElement: PsiElement, identifier: String) = {
    Option(PsiTreeUtil.findFirstParent(psiElement, DeclarationElementCondition)).
        map(_.asInstanceOf[HaskellDeclarationElement]).flatMap(declaration => {
      (typeInfo, declaration) match {
        case (None, dd: HaskellDataConstructorDeclarationElement) if dd.getSimpleType.getText == identifier =>
          dd.getIdentifierElements.find(_.getName == identifier).headOption
        case (Some(_), dd: HaskellDataConstructorDeclarationElement) if dd.getIdentifierElements.exists(_.getName == identifier) =>
          Some(dd.getIdentifierElements.filter(_.getName == identifier).max)
        case (_, d) => d.getIdentifierElements.find(_.getName == identifier)
      }
    })
  }

  private def resolveProjectReference(projectIdentifierInfo: ProjectIdentifierInfo, identifier: String): Option[HaskellNamedElement] = {
    for {
      filePath <- projectIdentifierInfo.filePath
      haskellFile <- findFile(filePath)
      offset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(projectIdentifierInfo.lineNr, projectIdentifierInfo.colNr))
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- {
        element match {
          case ne: HaskellNamedElement => Some(ne)
          case qvcoe: HaskellQVarConOpElement => Some(qvcoe.getIdentifierElement)
          case e => findQVarConOpElementParent(e).map(_.getIdentifierElement).
              orElse(PsiTreeUtil.findChildrenOfType(haskellFile, classOf[HaskellQVarConOpElement]).find(_.getTextOffset == offset).map(_.getIdentifierElement)).
              orElse(findNamedElementFromDeclaration(e, identifier))
        }
      }
    } yield namedElement
  }

  private def createBuiltInResolveResult(builtInIdentifierInfo: BuiltInIdentifierInfo, psiFile: PsiFile): ResolveResult = {
    new BuiltInResolveResult(builtInIdentifierInfo.typeSignature, builtInIdentifierInfo.libraryName, builtInIdentifierInfo.module)
  }

  private def findFile(filePath: String): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(FileUtil.makeFilePathAbsolute(filePath, myElement.getProject)))
    file.flatMap(f => Option(PsiManager.getInstance(myElement.getProject).findFile(f)).map(_.asInstanceOf[HaskellFile]))
  }

  private def resolveDeclarationReferencesInFile(file: PsiFile, identifier: String): Iterable[HaskellNamedElement] = {
    findNamedElementsEqualToIdentifier(findDeclarationElementsInFile(file).flatMap(_.getIdentifierElements), identifier)
  }

  private def findLocalNamedElements: Iterable[HaskellNamedElement] = {
    Option(PsiTreeUtil.getParentOfType(myElement, classOf[HaskellExpression])) match {
      case Some(p) => PsiTreeUtil.findChildrenOfType(p, classOf[HaskellNamedElement]).filterNot(_ == myElement)
      case None => Iterable()
    }
  }

  private def nextElementInExpression(element: PsiElement): Option[PsiElement] = {
     element.getNode.getElementType match {
       case HS_EXPRESSION => None
       case _ => Option(element.getNextSibling).flatMap(ns => Option(ns.getNextSibling)) // skip white space
    }
  }

  private def prevElementInExpression(element: PsiElement): Option[PsiElement] = {
    element.getNode.getElementType match {
      case HS_EXPRESSION => None
      case _ => Option(element.getPrevSibling).flatMap(ps => Option(ps.getPrevSibling)) // skip white space
    }
  }

  private def findLocalLhsElements(identifier: String) = {

    @tailrec
    def currentAndBackwardFind(identifier: String, lineExpression: HaskellLineExpressionElement): Option[HaskellNamedElement] = {
      findLocalLhsElementsInLineExpression(identifier, lineExpression) match {
        case None => prevElementInExpression(lineExpression) match {
          case Some(ne: HaskellLineExpressionElement) => currentAndBackwardFind(identifier, ne)
          case _ => None
        }
        case e => e
      }
    }

    @tailrec
    def forwardFind(identifier: String, lineExpression: HaskellLineExpressionElement): Option[HaskellNamedElement] = {
      nextElementInExpression(lineExpression) match {
        case Some(pe: HaskellLineExpressionElement) => findLocalLhsElementsInLineExpression(identifier, pe) match {
          case None => forwardFind(identifier, pe)
          case e => e
        }
        case _ => None
      }
    }

    val currentLineExpression = Option(PsiTreeUtil.getParentOfType(myElement, classOf[HaskellLineExpressionElement]))
    currentLineExpression.flatMap(cle => currentAndBackwardFind(identifier, cle).orElse(forwardFind(identifier, cle)))
  }

  private def findLocalLhsElementsInLineExpression(identifier: String, lineExpression: HaskellLineExpressionElement): Option[HaskellNamedElement] = {
    val localElements = PsiTreeUtil.findChildrenOfType(lineExpression, classOf[HaskellQVarConOpElement])
    val sameNameElements = localElements.filter(_.getIdentifierElement.getName == identifier)
    val result = sameNameElements.find { e =>
      isNextSiblingLhsIndicator(e) ||
          isDirectSiblingConstructor(e) ||
          isPreviousDirectSiblingBackslash(e)
    }
    result.map(_.getIdentifierElement)
  }

  private def isNextSiblingLhsIndicator(e: PsiElement): Boolean = {
    val nextSibling = Option(e.getNextSibling)
    nextSibling match {
      case Some(ns) if IndicatingLhsTypes.contains(ns.getNode.getElementType) =>
        true
      case Some(ns) if IgnoringTypes.contains(ns.getNode.getElementType) =>
        isNextSiblingLhsIndicator(ns)
      case Some(ns) if ns.getNode.getElementType == HS_SNL =>
        val nextLineExpressionFirstChild = for {
          p <- Option(ns.getParent)
          ne <- nextElementInExpression(p)
          c <- Option(ne.getFirstChild)
        } yield c

        nextLineExpressionFirstChild match {
          case Some(nle) if IndicatingLhsTypes.contains(nle.getNode.getElementType) => true
          case Some(nle) if IgnoringTypes.contains(nle.getNode.getElementType) => isNextSiblingLhsIndicator(nle)
          case _ => false
        }
      case _ =>
        false
    }
  }

  private def isDirectSiblingConstructor(e: PsiElement): Boolean = {
    val next = nextElementInExpression(e)
    (next match {
      case Some(n) if LhsConstructors.contains(n.getNode.getElementType) => true
      case _ => false
    }) || {
      val previous = prevElementInExpression(e)
      previous match {
        case Some(n) if LhsConstructors.contains(n.getNode.getElementType) => true
        case _ => false
      }
    }
  }

  private def isPreviousDirectSiblingBackslash(e: PsiElement): Boolean = {
    val previous = Option(e.getPrevSibling)
    previous match {
      case Some(p) if p.findElementAt(0).getNode.getElementType == HS_BACKSLASH => true
      case Some(p) if p.getNode.getElementType == TokenType.WHITE_SPACE | p.getNode.getElementType == HS_QVAR | p.findElementAt(0).getNode.getElementType == HS_UNDERSCORE => isPreviousDirectSiblingBackslash(p)
      case _ => false
    }
  }

  private def findDeclarationElementsInFile(file: PsiFile): Iterable[HaskellDeclarationElement] = {
    PsiTreeUtil.findChildrenOfType(file, classOf[HaskellDeclarationElement])
  }

  private def findNamedElementsEqualToIdentifier(namedElements: Iterable[HaskellNamedElement], identifier: String): Iterable[HaskellNamedElement] = {
    namedElements.filter(_.getName == identifier)
  }

  private def getIdentifierInfos(psiFile: PsiFile, namedElement: HaskellNamedElement): Iterable[IdentifierInfo] = {
    GhcModInfo.findInfoFor(psiFile, namedElement)
  }

  private def findQVarConOpElementParent(element: PsiElement) = {
    Option(PsiTreeUtil.findFirstParent(element, HaskellElementCondition.QVarConOpElementCondition)).map(_.asInstanceOf[HaskellQVarConOpElement])
  }
}

class HaskellGlobalResolveResult(val element: HaskellNamedElement) extends PsiElementResolveResult(element)

class HaskellFileResolveResult(val element: PsiElement) extends PsiElementResolveResult(element)

class HaskellLocalResolveResult(val element: HaskellNamedElement) extends PsiElementResolveResult(element)

object HaskellReference {

  import com.powertuple.intellij.haskell.psi.HaskellTypes._

  private final val IgnoringTypes = Seq(TokenType.WHITE_SPACE, HS_QVAR, HS_LEFT_PAREN, HS_RIGHT_PAREN, HS_COMMA, HS_RIGHT_BRACKET, HS_QCON, HS_QCON_OP)
  private final val IndicatingLhsTypes = Seq(HS_EQUAL, HS_VERTICAL_BAR, HS_LEFT_ARROW, HS_RIGHT_ARROW)
  private final val LhsConstructors = Seq(HS_QCON, HS_QCON_OP)
}