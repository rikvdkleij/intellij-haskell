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
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.{HLintComponent, HLintInfo}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util._

import scala.annotation.tailrec
import scala.concurrent.Future

class HLintInspectionTool extends LocalInspectionTool {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"HLint inspection is started for file ${psiFile.getName}")

    ProgressManager.checkCanceled()

    HaskellFileUtil.findDocument(psiFile) match {
      case Some(document) if HaskellProjectUtil.isSourceFile(psiFile) =>

        ProgressManager.checkCanceled()

        new WaitFor(100, 1) {
          override def condition(): Boolean = {
            ProgressManager.checkCanceled()
            !HaskellFileUtil.isDocumentUnsaved(document)
          }
        }

        ProgressManager.checkCanceled()

        val result = ScalaFutureUtil.waitWithCheckCancelled(psiFile.getProject, Future(HLintComponent.check(psiFile)), "Running HLint", timeout = 2.seconds) match {
          case Some(r) => r
          case None => Seq()
        }

        val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)

        ProgressManager.checkCanceled()

        for {
          hi <- result
          problemType = findProblemHighlightType(hi)
          if problemType != ProblemHighlightType.GENERIC_ERROR
          () = ProgressManager.checkCanceled()
          vf <- HaskellFileUtil.findVirtualFile(psiFile)
          () = ProgressManager.checkCanceled()
          se <- findStartHaskellElement(vf, psiFile, hi)
          () = ProgressManager.checkCanceled()
          ee <- findEndHaskellElement(vf, psiFile, hi)
          sl <- fromOffset(vf, se)
          () = ProgressManager.checkCanceled()
          el <- fromOffset(vf, ee)
        } yield {
          ProgressManager.checkCanceled()
          hi.to match {
            case Some(to) if se.isValid && ee.isValid =>
              problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(createQuickfix(hi, se, ee, sl, el, to)), problemType, false, null, true, isOnTheFly))
            case None =>
              problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(), problemType, false, null, true, isOnTheFly))
            case _ => ()
          }
        }

        HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"HLint inspection is finished for file ${psiFile.getName}")

        if (result.isEmpty) {
          null
        } else {
          problemsHolder.getResultsArray
        }
      case _ => null
    }
  }

  private def createQuickfix(hLintInfo: HLintInfo, startElement: PsiElement, endElement: PsiElement, startLineNumber: Int, endLineNumber: Int, to: String) = {
    new HLintQuickfix(startElement, endElement, hLintInfo.startLine, hLintInfo.startColumn, removeLineBreaksAndExtraSpaces(startLineNumber, endLineNumber, to), hLintInfo.hint, hLintInfo.note)
  }

  private def fromOffset(virtualFile: VirtualFile, psiElement: PsiElement): Option[Int] = {
    LineColumnPosition.fromOffset(virtualFile, psiElement.getTextOffset).map(_.lineNr)
  }

  private def removeLineBreaksAndExtraSpaces(sl: Int, el: Int, s: String) = {
    if (sl == el) {
      s.replaceAll("""\n""", " ").replaceAll("""\s+""", " ")
    } else {
      s
    }
  }

  private def findStartHaskellElement(virtualFile: VirtualFile, psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
    val offset = LineColumnPosition.getOffset(virtualFile, LineColumnPosition(hlintInfo.startLine, hlintInfo.startColumn))
    ProgressManager.checkCanceled()
    val element = offset.flatMap(offset => Option(psiFile.findElementAt(offset)))
    ProgressManager.checkCanceled()
    element.filterNot(e => HLintInspectionTool.NotHaskellIdentifiers.contains(e.getNode.getElementType))
  }

  private def findEndHaskellElement(virtualFile: VirtualFile, psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
    val endOffset = if (hlintInfo.endLine >= hlintInfo.startLine && hlintInfo.endColumn > hlintInfo.startColumn) {
      LineColumnPosition.getOffset(virtualFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn - 1))
    } else {
      LineColumnPosition.getOffset(virtualFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn))
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
  val NotHaskellIdentifiers: Seq[IElementType] = Seq(HS_NEWLINE, HS_COMMENT, HS_NCOMMENT, TokenType.WHITE_SPACE, HS_HADDOCK, HS_NHADDOCK)
}