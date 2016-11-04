/*
 * Copyright 2016 Rik van der Kleij
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

package intellij.haskell.inspection

import com.intellij.codeInspection._
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import intellij.haskell.external.component.{HLintComponent, HLintInfo}
import intellij.haskell.psi.HaskellTypes.{HS_COMMENT, HS_NCOMMENT, HS_NEWLINE}
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

import scala.annotation.tailrec

class HLintInspectionTool extends LocalInspectionTool {

  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    if (HaskellProjectUtil.isLibraryFile(psiFile)) {
      return Array()
    }

    val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)
    val hlintInfos = HLintComponent.check(psiFile)
    for {
      hi <- hlintInfos
      se <- findStartHaskellElement(psiFile, hi)
      ee <- findEndHaskellElement(psiFile, hi)
    } yield
      hi.to match {
        case Some(to) => problemsHolder.registerProblem(
          new ProblemDescriptorBase(se, ee, hi.hint, Array(new HLintQuickfix(se, ee, hi.startLine, hi.startColumn, to, hi.note)), findProblemHighlightType(hi), false, null, true, isOnTheFly)
        )
        case None => problemsHolder.registerProblem(
          new ProblemDescriptorBase(se, ee, hi.hint, Array(), findProblemHighlightType(hi), false, null, true, isOnTheFly))
      }
    problemsHolder.getResultsArray
  }

  private def findStartHaskellElement(psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
    val offset = LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.startLine, hlintInfo.startColumn))
    val element = offset.flatMap(offset => Option(psiFile.findElementAt(offset)))
    element.filterNot(e => HLintInspectionTool.NotHaskellIdentifiers.contains(e.getNode.getElementType))
  }

  private def findEndHaskellElement(psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
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
      case None => findHaskellIdentifier(psiFile, offset - 1)
      case Some(e) if HLintInspectionTool.NotHaskellIdentifiers.contains(e.getNode.getElementType) => findHaskellIdentifier(psiFile, offset - 1)
      case e => e
    }
  }

  private def findProblemHighlightType(hlintInfo: HLintInfo) = {
    hlintInfo.severity match {
      case "Warning" => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
      case "Error" => ProblemHighlightType.GENERIC_ERROR
      case _ => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    }
  }
}

object HLintInspectionTool {
  val NotHaskellIdentifiers = Seq(HS_NEWLINE, HS_COMMENT, HS_NCOMMENT, TokenType.WHITE_SPACE)
}