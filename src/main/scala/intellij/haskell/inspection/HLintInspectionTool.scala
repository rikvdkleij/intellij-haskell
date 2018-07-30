/*
 * Copyright 2014-2018 Rik van der Kleij
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.{HLintComponent, HLintInfo}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition, ScalaUtil}

import scala.annotation.tailrec

class HLintInspectionTool extends LocalInspectionTool {

  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    ProgressManager.checkCanceled()

    if (HaskellProjectUtil.isLibraryFile(psiFile)) {
      return Array()
    }

    ProgressManager.checkCanceled()

    val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)

    ProgressManager.checkCanceled()

    val hlintCheckFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable[Seq[HLintInfo]] {
      HLintComponent.check(psiFile)
    })

    ProgressManager.checkCanceled()

    val wf = new WaitFor(5000, 1) {
      override def condition(): Boolean = {
        ProgressManager.checkCanceled()
        hlintCheckFuture.isDone
      }
    }

    val result = if (wf.isConditionRealized) {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"Running Hlint is finished for file ${psiFile.getName}")
      hlintCheckFuture.get()
    } else {
      HaskellNotificationGroup.logErrorEvent(psiFile.getProject, s"Timeout while running HLint for file ${psiFile.getName}")
      Seq()
    }

    for {
      hi <- result
      problemType = findProblemHighlightType(hi)
      if problemType != ProblemHighlightType.GENERIC_ERROR
      se <- findStartHaskellElement(psiFile, hi)
      ee <- findEndHaskellElement(psiFile, hi)
      sl <- fromOffset(psiFile, se)
      el <- fromOffset(psiFile, ee)
    } yield {
      ProgressManager.checkCanceled()
      hi.to match {
        case Some(to) if se.isValid && ee.isValid =>
          problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(createQuickfix(hi, se, ee, sl, el, to)), problemType, false, null, true, isOnTheFly))
        case None =>
          ProgressManager.checkCanceled()
          problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(), problemType, false, null, true, isOnTheFly))
        case _ => ()
      }
    }
    HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"HLint inspection is finished for file ${psiFile.getName}")
    problemsHolder.getResultsArray
  }

  private def createQuickfix(hLintInfo: HLintInfo, startElement: PsiElement, endElement: PsiElement, startLineNumber: Int, endLineNumber: Int, to: String) = {
    new HLintQuickfix(startElement, endElement, hLintInfo.startLine, hLintInfo.startColumn, removeLineBreaksAndExtraSpaces(startLineNumber, endLineNumber, to), hLintInfo.hint, hLintInfo.note)
  }

  private def fromOffset(psiFile: PsiFile, psiElement: PsiElement): Option[Int] = {
    LineColumnPosition.fromOffset(psiFile, psiElement.getTextOffset).map(_.lineNr)
  }

  private def removeLineBreaksAndExtraSpaces(sl: Int, el: Int, s: String) = {
    if (sl == el) {
      s.replaceAll("""\n""", " ").replaceAll("""\s+""", " ")
    } else {
      s
    }
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
  val NotHaskellIdentifiers = Seq(HS_NEWLINE, HS_COMMENT, HS_NCOMMENT, TokenType.WHITE_SPACE, HS_HADDOCK, HS_NHADDOCK)
}