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

class HaskellVarReference(element: HaskellVar, textRange: TextRange) extends PsiReferenceBase[HaskellVar](element, textRange) {

  override def resolve: PsiElement = {
    val psiFile = myElement.getContainingFile

    // Skip library files
    if (!ProjectUtil.isProjectFile(psiFile)) {
      return null;
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
    } yield PsiUtilCore.getElementAtOffset(haskellFile, startOffset).getParent.asInstanceOf[HaskellVar]).orNull
  }

  /**
   * Did not find solution to take scope into account, so currently it returns all vars of file :-(
   */
  override def getVariants: Array[AnyRef] = {
    val haskellFile = myElement.getContainingFile.asInstanceOf[HaskellFile]
    val vars = PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellVar]).filter(v => v != null && v.getName != null && !v.getName.isEmpty).groupBy(_.getName).map(_._2.head).toArray
    vars.map(v => LookupElementBuilder.create(v).withIcon(HaskellIcons.HASKELL_SMALL_LOGO).withTypeText(v.getName))
  }

  private def getProjectExpressionInfo(expressionInfo: Option[ExpressionInfo]) = {
    expressionInfo match {
      case Some(lei: ProjectExpressionInfo) => Some(lei)
      case _ => None
    }
  }

  private def findTypeSignaturesFor(haskellFile: HaskellFile, expression: String): Option[PsiElement] = {
    Option(PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellStartTypeSignature])) match {
      //      case Some(typeSignatures) => typeSignatures.map(ts => ts.getFirstChild).find(v => v.getText == expression)
      case Some(typeSignatures) => typeSignatures.find(v => v.getIdentifier == expression).map(_.getNameIdentifier)
      case _ => None
    }
  }

  private def getExpressionInfo(psiFile: PsiFile, expression: String): Option[ExpressionInfo] = {
    GhcModiManager.getInstance(psiFile.getProject).findInfoFor(psiFile, expression)
  }
}