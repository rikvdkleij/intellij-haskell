/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.util.{PsiTreeUtil, PsiUtilCore}
import com.powertuple.intellij.haskell.external.{ExpressionInfo, GhcModiManager, ProjectExpressionInfo}
import com.powertuple.intellij.haskell.util.{FileUtil, LineColumnPosition, ProjectUtil}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons}

import scala.collection.JavaConversions._

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val psiFile = myElement.getContainingFile

    // Skip library files because ghc-mod(i) will not support them.
    if (!ProjectUtil.isProjectFile(psiFile)) {
      return null
    }

    FileUtil.saveAllFiles()

    val expression = myElement.getText.substring(textRange.getStartOffset, textRange.getEndOffset)

    (for {
      expressionInfo <- getExpressionInfo(psiFile, expression)
      haskellFile <- Option(PsiManager.getInstance(myElement.getProject).
          findFile(LocalFileSystem.getInstance().findFileByPath(expressionInfo.filePath)).asInstanceOf[HaskellFile])
      typeSignature <- findTypeSignaturesFor(haskellFile, expression)
    } yield typeSignature).orElse(for {
      expressionInfo <- getProjectExpressionInfo(getExpressionInfo(psiFile, expression))
      haskellFile <- Option(PsiManager.getInstance(myElement.getProject).
          findFile(LocalFileSystem.getInstance().findFileByPath(expressionInfo.filePath)).asInstanceOf[HaskellFile])
      startOffset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(expressionInfo.lineNr, expressionInfo.colNr))
    } yield PsiUtilCore.getElementAtOffset(haskellFile, startOffset)).orNull
  }

  override def getVariants: Array[AnyRef] = {
    val haskellFile = myElement.getContainingFile.asInstanceOf[HaskellFile]
    val declarations = PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellDeclarationElement])

    declarations.map {
      case d: HaskellDataDeclaration => Seq(createLookupElement(d)) ++ d.getConstrs.getConstrList.map(e => createLookupElement(e))
      case d => Seq(createLookupElement(d))
    }.flatten.asInstanceOf[Array[AnyRef]]
  }

  private def createLookupElement(declarationElement: HaskellDeclarationElement) = {
    LookupElementBuilder.create(declarationElement.getIdentifier).withTypeText(declarationElement.getText).withIcon(HaskellIcons.HASKELL_SMALL_LOGO)
  }

  private def createLookupElement(constr: HaskellConstr) = {
    LookupElementBuilder.create(constr.getIdentifier).withTypeText(constr.getText).withIcon(HaskellIcons.HASKELL_SMALL_LOGO)
  }

  private def getProjectExpressionInfo(expressionInfo: Option[ExpressionInfo]) = {
    expressionInfo match {
      case Some(lei: ProjectExpressionInfo) => Some(lei)
      case _ => None
    }
  }

  private def findTypeSignaturesFor(haskellFile: HaskellFile, expression: String): Option[HaskellNamedElement] = {
    Option(PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellTypeSignature])) match {
      case Some(typeSignatures) => typeSignatures.find(v => v.getIdentifier == expression).map(_.getIdentifierElement)
      case _ => None
    }
  }

  private def getExpressionInfo(psiFile: PsiFile, expression: String): Option[ExpressionInfo] = {
    GhcModiManager.findInfoFor(psiFile, expression)
  }
}