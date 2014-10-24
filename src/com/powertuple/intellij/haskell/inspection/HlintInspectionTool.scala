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
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.LineColumnPosition

class HlintInspectionTool extends LocalInspectionTool {
  override def checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val problemsHolder = new ProblemsHolder(manager, file, isOnTheFly)
    if (HaskellSettings.getInstance().getState.hlintPath.isEmpty) {
      HaskellNotificationGroup.notifyWarning("Can not run HLint. Configure path to HLint in Haskell Settings")
      problemsHolder.getResultsArray
    } else {
      val hlintInfos = Hlint.check(file)
      for {
        hi <- hlintInfos
        offSet <- LineColumnPosition.getOffset(file, LineColumnPosition(hi.startLine, hi.startColumn))
        e <- Option(file.findElementAt(offSet))
      } yield problemsHolder.registerProblem(e, hi.hint, findProblemHighlightType(hi))
      problemsHolder.getResultsArray
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
