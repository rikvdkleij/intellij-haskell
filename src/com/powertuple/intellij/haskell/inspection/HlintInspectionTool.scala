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

package com.powertuple.intellij.haskell.inspection

import com.intellij.codeInspection._
import com.intellij.psi.PsiFile
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.external.{Hlint, HlintInfo}
import com.powertuple.intellij.haskell.psi.HaskellTypes.{HS_NEWLINE, HS_SNL}
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.LineColumnPosition

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
        startOffSet <- LineColumnPosition.getOffset(psiFile, LineColumnPosition(hi.startLine, hi.startColumn))
        endOffSet <- LineColumnPosition.getOffset(psiFile, LineColumnPosition(hi.endLine, hi.endColumn - 1))
        se <- Option(psiFile.findElementAt(startOffSet))
        ee <- findLastHaskellElement(psiFile, endOffSet)
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

  private def findLastHaskellElement(psiFile: PsiFile, offset: Int) = {
    Option(psiFile.findElementAt(offset)) match {
      case Some(e) if e.getNode.getElementType == HS_NEWLINE || e.getNode.getElementType == HS_SNL => Option(psiFile.findElementAt(offset - 1))
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
