/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.inspection

import com.intellij.codeInspection._
import com.intellij.psi.{TokenType, PsiElement, PsiFile}
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.external.{Hlint, HlintInfo}
import com.powertuple.intellij.haskell.psi.HaskellTypes.{HS_COMMENT, HS_NCOMMENT, HS_NEWLINE, HS_SNL}
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.LineColumnPosition

import scala.annotation.tailrec

class HlintInspectionTool extends LocalInspectionTool {
  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)
    if (HaskellSettings.getInstance().getState.hlintPath.isEmpty) {
      HaskellNotificationGroup.notifyWarning("Can not run HLint. Configure path to HLint in Haskell Settings")
      problemsHolder.getResultsArray
    } else {
      val hlintInfos = Hlint.check(psiFile)
      for {
        hi <- hlintInfos
        se <- findStartHaskellElement(psiFile, hi)
        ee <- findEndHaskellElement(psiFile, hi)
      } yield
        hi.to match {
          case Some(to) => problemsHolder.registerProblem(
            new ProblemDescriptorBase(se, ee, hi.hint, Array(new HlintQuickfix(se, ee, to, hi.note)), findProblemHighlightType(hi), false, null, true, isOnTheFly))
          case None => problemsHolder.registerProblem(
            new ProblemDescriptorBase(se, ee, hi.hint, Array(), findProblemHighlightType(hi), false, null, true, isOnTheFly))
        }
      problemsHolder.getResultsArray
    }
  }

  private def findStartHaskellElement(psiFile: PsiFile, hlintInfo: HlintInfo) = {
    val startOffset = LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.startLine, hlintInfo.startColumn))
    val element = startOffset.flatMap(offset => Option(psiFile.findElementAt(offset)))
    element.filterNot(HlintInspectionTool.NotHaskellIdentifiers.contains(_))
  }

  private def findEndHaskellElement(psiFile: PsiFile, hlintInfo: HlintInfo): Option[PsiElement] = {
    val endOffset = if (hlintInfo.endLine >= hlintInfo.startLine && hlintInfo.endColumn > hlintInfo.startColumn) {
      LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn - 1))
    } else {
      LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn))
    }
    endOffset.flatMap(offset => findHaskellIdentifier(psiFile, offset))
  }

  @tailrec
  private def findHaskellIdentifier(psiFile: PsiFile, offset: Int): Option[PsiElement] = {
    Option(psiFile.findElementAt(offset)) match {
      case Some(e) if HlintInspectionTool.NotHaskellIdentifiers.contains(e.getNode.getElementType) => findHaskellIdentifier(psiFile, offset - 1)
      case maybeElement => maybeElement
    }
  }

  private def findProblemHighlightType(hlintInfo: HlintInfo) = {
    hlintInfo.severity match {
      case "Warning" => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
      case "Error" => ProblemHighlightType.ERROR
      case _ => ProblemHighlightType.INFORMATION
    }
  }
}

object HlintInspectionTool {
  val NotHaskellIdentifiers = Seq(HS_NEWLINE, HS_SNL, HS_COMMENT, HS_NCOMMENT, TokenType.WHITE_SPACE)
}