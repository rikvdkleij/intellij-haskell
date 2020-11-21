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

package intellij.haskell.inspection

import com.intellij.codeInspection._
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HLintRefactoringsParser._
import intellij.haskell.external.component.{HLintComponent, HLintInfo, HLintRefactoringsParser}
import intellij.haskell.util._

import scala.concurrent.duration._
import scala.concurrent.{Future, blocking}

class HLintInspectionTool extends LocalInspectionTool {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val project = psiFile.getProject
    HaskellNotificationGroup.logInfoEvent(project, s"HLint inspection is started for file ${psiFile.getName}")

    ProgressManager.checkCanceled()

    HaskellFileUtil.findDocument(psiFile) match {
      case Some(document) if HaskellProjectUtil.isSourceFile(psiFile) =>

        ProgressManager.checkCanceled()

        new WaitFor(500, 1) {
          override def condition(): Boolean = {
            ProgressManager.checkCanceled()
            !HaskellFileUtil.isDocumentUnsaved(document)
          }
        }

        ProgressManager.checkCanceled()

        val hlintInfos = ScalaFutureUtil.waitForValue(project,
          Future {
            blocking {
              HLintComponent.check(psiFile)
            }
          }, "Running HLint", timeout = 2.seconds) match {
          case Some(r) => r
          case None => Seq()
        }

        val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)

        for {
          hlintInfo <- hlintInfos
          problemType = findProblemHighlightType(hlintInfo)
          if problemType != ProblemHighlightType.GENERIC_ERROR
          virtualFile <- HaskellFileUtil.findVirtualFile(psiFile)
        } yield {
          ProgressManager.checkCanceled()

          val quickFix = if (hlintInfo.refactorings == "[]") {
            HaskellNotificationGroup.logWarningEvent(project, s"No HLint refactorings for: ${hlintInfo.from} | ${hlintInfo.hint}")
            None
          } else {
            HLintRefactoringsParser.parseRefactoring(project, hlintInfo.refactorings) match {
              case Some(Delete(rType, pos)) => None
              case Some(Replace(rType, pos, subts, orig)) => createReplaceQuickfix(document, virtualFile, psiFile, rType, pos, subts, orig, hlintInfo.hint, hlintInfo.note)
              case _ => None
            }
          }

          quickFix match {
            case Some(qf) => problemsHolder.registerProblem(new ProblemDescriptorBase(qf.getStartElement, qf.getEndElement, hlintInfo.hint, Array(qf), problemType, false, null, true, isOnTheFly))
            case None => ManualHLintQuickfix.registerProblem(psiFile, virtualFile, hlintInfo, problemsHolder, problemType, isOnTheFly)
          }
        }

        HaskellNotificationGroup.logInfoEvent(project, s"HLint inspection is finished for file ${psiFile.getName}")

        if (hlintInfos.isEmpty) {
          null
        } else {
          problemsHolder.getResultsArray
        }
      case _ => null
    }
  }

  private def findTextWithOffsets(virtualFile: VirtualFile, document: Document, pos: SrcSpan) = {
    for {
      startOffset <- LineColumnPosition.getOffset(virtualFile, LineColumnPosition(pos.startLine, pos.startCol))
      endOffset <- LineColumnPosition.getOffset(virtualFile, LineColumnPosition(pos.endLine, pos.endCol))
      text = document.getText(TextRange.create(startOffset, endOffset))
    } yield (startOffset, endOffset, text)
  }

  private def findText(virtualFile: VirtualFile, document: Document, pos: SrcSpan) =
    findTextWithOffsets(virtualFile, document, pos).map(_._3)

  private def createReplaceQuickfix(document: Document, virtualFile: VirtualFile, psiFile: PsiFile, rType: RType, pos: SrcSpan, subts: Subts, orig: String, hint: String, note: Seq[String]) = {
    for {
      (replaceStartOffset, replaceEndOffset, _) <- findTextWithOffsets(virtualFile, document, pos)
      startElement <- Option(psiFile.findElementAt(replaceStartOffset))
      endElement <- Option(psiFile.findElementAt(replaceEndOffset - 1))
      newText = subts.map({ case (x, pos) => (x, findText(virtualFile, document, pos)) }).collect {
        case (w, Some(toReplace)) => (w, toReplace)
      }.foldLeft(orig)({ case (x, y) => x.replace(y._1, y._2) })
    } yield new HLintQuickfix(virtualFile, startElement, endElement, replaceStartOffset, replaceEndOffset, newText, hint, note)
  }

  private def findProblemHighlightType(hlintInfo: HLintInfo) = hlintInfo.severity match {
    case "Warning" => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    case "Error" => ProblemHighlightType.GENERIC_ERROR
    case _ => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
  }
}