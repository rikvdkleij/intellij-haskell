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
import com.intellij.openapi.util.{Condition, TextRange}
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.util.{PsiTreeUtil, PsiUtilCore}
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.{HaskellEditorUtil, LineColumnPosition}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons}

import scala.collection.JavaConversions._

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val file = myElement.getContainingFile

    val expression = myElement.getText.substring(textRange.getStartOffset, textRange.getEndOffset)

    val resolveResultsByGhcMod = getExpressionInfos(file, expression).map {
      case pei: ProjectExpressionInfo => createResolveResults(pei, expression)
      case lei: LibraryExpressionInfo => createResolveResults(lei, expression)
      case bie: BuiltInExpressionInfo => createResolveResults(bie, file)
    }.flatten

    val firstBuiltInResolveResult = resolveResultsByGhcMod.find(_.isInstanceOf[BuiltInResolveResult])
    firstBuiltInResolveResult match {
      case Some(birs: BuiltInResolveResult) => HaskellEditorUtil.createLabelMessage(s"${birs.typeSignature} is built-in: ${birs.libraryName}:${birs.module}", file.getProject)
      case _ => ()
    }

    (if (resolveResultsByGhcMod.isEmpty) {
      findResolveResultInFile(file, expression)
    } else {
      resolveResultsByGhcMod
    }).toArray
  }

  override def getVariants: Array[AnyRef] = {
    val file = myElement.getContainingFile
    val expressionParent = Option(PsiTreeUtil.findFirstParent(element, expressionCondition))
    val namedElements = expressionParent.map(PsiTreeUtil.findChildrenOfType(_, classOf[HaskellNamedElement]).map(createLookupElement).toIterable).getOrElse(Iterable())

    val declarationNamedElements = PsiTreeUtil.findChildrenOfType(file, classOf[HaskellDeclarationElement]).flatMap(createLookupElement)
    (declarationNamedElements ++ namedElements).groupBy(_.getLookupString).values.map(_.head).toArray
  }

  private final val expressionCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellExpression => true
        case _ => false
      }
    }
  }

  private def createLookupElement(namedElement: HaskellNamedElement) = {
    LookupElementBuilder.create(namedElement.getName).withIcon(HaskellIcons.HaskellSmallLogo)
  }

  private def createLookupElement(declarationElement: HaskellDeclarationElement) = {
    declarationElement.getIdentifierElements.map(ne => LookupElementBuilder.create(ne.getName).withTypeText(declarationElement.getPresentation.getPresentableText).withIcon(declarationElement.getPresentation.getIcon(false)))
  }

  private def createResolveResults(libraryExpressionInfo: LibraryExpressionInfo, expression: String): Iterable[ResolveResult] = {
    findFile(libraryExpressionInfo.filePath).map(findResolveResultInFile(_, expression)).getOrElse(Iterable())
  }

  private def createResolveResults(projectExpressionInfo: ProjectExpressionInfo, expression: String): Iterable[ResolveResult] = {
    findFile(projectExpressionInfo.filePath).map(file => findResolveResultInFile(file, expression)) match {
      case None => createResolveResultsByUsingLineColumnInfo(projectExpressionInfo)
      case Some(rr) if rr.isEmpty => createResolveResultsByUsingLineColumnInfo(projectExpressionInfo)
      case Some(rr) => rr
    }
  }

  private def createResolveResultsByUsingLineColumnInfo(projectExpressionInfo: ProjectExpressionInfo): Iterable[ResolveResult] = {
    (for {
      haskellFile <- findFile(projectExpressionInfo.filePath)
      startOffset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(projectExpressionInfo.lineNr, projectExpressionInfo.colNr))
    } yield new PsiElementResolveResult(PsiUtilCore.getElementAtOffset(haskellFile, startOffset)).asInstanceOf[ResolveResult]).map(Iterable(_)).getOrElse(Iterable())
  }

  private def createResolveResults(builtInExpressionInfo: BuiltInExpressionInfo, psiFile: PsiFile): Iterable[ResolveResult] = {
    Iterable(new BuiltInResolveResult(builtInExpressionInfo.typeSignature, builtInExpressionInfo.libraryName, builtInExpressionInfo.module))
  }

  private class BuiltInResolveResult(val typeSignature: String, val libraryName: String, val module: String) extends ResolveResult {
    override def getElement: PsiElement = null

    override def isValidResult: Boolean = false
  }

  private def findFile(filePath: String): Option[HaskellFile] = {
    Option(PsiManager.getInstance(myElement.getProject).findFile(LocalFileSystem.getInstance().findFileByPath(filePath)).asInstanceOf[HaskellFile])
  }

  private def findResolveResultInFile(file: PsiFile, expression: String): Iterable[ResolveResult] = {
    PsiTreeUtil.findChildrenOfType(file, classOf[HaskellDeclarationElement])
        .map(de => findNamedElements(de, expression)).flatMap(hnes => hnes.map(new PsiElementResolveResult(_)))
  }

  private def findNamedElements(declarationElement: HaskellDeclarationElement, expression: String): Seq[HaskellNamedElement] = {
    declarationElement.getIdentifierElements.filter(_.getName == expression)
  }

  private def getExpressionInfos(psiFile: PsiFile, expression: String): Seq[ExpressionInfo] = {
    GhcModiManager.findInfoFor(psiFile, expression)
  }
}