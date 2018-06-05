/*
 * Copyright 2014-2017 Rik van der Kleij
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

import java.util.concurrent.Callable

import com.intellij.codeInspection._
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HLintComponent.HLintName
import intellij.haskell.external.component.{HLintComponent, HLintInfo, StackProjectManager}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

import scala.annotation.tailrec

class HLintInspectionTool extends LocalInspectionTool {


  override def checkFile(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    ProgressManager.checkCanceled()

    if (!StackProjectManager.isHlintAvailable(psiFile.getProject)) {
      HaskellNotificationGroup.logInfoBalloonEvent(psiFile.getProject, s"$HLintName is not yet available")
      return Array()
    }

    if (HaskellProjectUtil.isLibraryFile(psiFile)) {
      return Array()
    }

    ProgressManager.checkCanceled()

    val problemsHolder = new ProblemsHolder(manager, psiFile, isOnTheFly)

    ProgressManager.checkCanceled()

    val hlintInfosFuture = ApplicationManager.getApplication.executeOnPooledThread(new Callable[Array[ProblemDescriptor]] {
      override def call(): Array[ProblemDescriptor] = {
        ProgressManager.checkCanceled()
        val result = HLintComponent.check(psiFile)
        ProgressManager.checkCanceled()
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
          ApplicationManager.getApplication.runReadAction(new Runnable() {
            override def run(): Unit = {
              ProgressManager.checkCanceled()
              hi.to match {
                case Some(to) =>
                  problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(createQuickfix(hi, se, ee, sl, el, to)), problemType, false, null, true, isOnTheFly))
                case None =>
                  ProgressManager.checkCanceled()
                  problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hi.hint, Array(), problemType, false, null, true, isOnTheFly))
              }
            }
          })
        }
        problemsHolder.getResultsArray
      }
    })

    ProgressManager.checkCanceled()

    new WaitFor(60000, 5) {
      override def condition(): Boolean = {
        ProgressManager.checkCanceled()
        hlintInfosFuture.isDone
      }
    }

    hlintInfosFuture.get()
  }

  private def createQuickfix(hLintInfo: HLintInfo, startElement: PsiElement, endElement: PsiElement, startLineNumber: Int, endLineNumber: Int, to: String) = {
    new HLintQuickfix(startElement, endElement, hLintInfo.startLine, hLintInfo.startColumn, removeLineBreaksAndExtraSpaces(startLineNumber, endLineNumber, to), hLintInfo.hint, hLintInfo.note)
  }

  private def fromOffset(psiFile: PsiFile, psiElement: PsiElement): Option[Int] = {
    ProgressManager.checkCanceled()

    ApplicationManager.getApplication.runReadAction(new Computable[Option[Int]] {
      override def compute(): Option[Int] = {
        LineColumnPosition.fromOffset(psiFile, psiElement.getTextOffset).map(_.lineNr)
      }
    })
  }

  private def removeLineBreaksAndExtraSpaces(sl: Int, el: Int, s: String) = {
    if (sl == el) {
      s.replaceAll("""\n""", " ").replaceAll("""\s+""", " ")
    } else {
      s
    }
  }

  private def findStartHaskellElement(psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
    ProgressManager.checkCanceled()

    val element = ApplicationManager.getApplication.runReadAction(new Computable[Option[PsiElement]] {
      override def compute(): Option[PsiElement] = {
        val offset = LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.startLine, hlintInfo.startColumn))
        offset.flatMap(offset => Option(psiFile.findElementAt(offset)))
      }
    })

    element.filterNot(e => HLintInspectionTool.NotHaskellIdentifiers.contains(e.getNode.getElementType))
  }

  private def findEndHaskellElement(psiFile: PsiFile, hlintInfo: HLintInfo): Option[PsiElement] = {
    ProgressManager.checkCanceled()

    ApplicationManager.getApplication.runReadAction(new Computable[Option[PsiElement]] {
      override def compute(): Option[PsiElement] = {
        val endOffset = if (hlintInfo.endLine >= hlintInfo.startLine && hlintInfo.endColumn > hlintInfo.startColumn) {
          LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn - 1))
        } else {
          LineColumnPosition.getOffset(psiFile, LineColumnPosition(hlintInfo.endLine, hlintInfo.endColumn))
        }
        endOffset.flatMap(offset => findHaskellIdentifier(psiFile, offset))
      }
    })
  }

  @tailrec
  private def findHaskellIdentifier(psiFile: PsiFile, offset: Int): Option[PsiElement] = {
    ProgressManager.checkCanceled()

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