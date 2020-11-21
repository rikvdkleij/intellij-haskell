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
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HLintInfo
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi.{HaskellElementFactory, HaskellTypes}
import intellij.haskell.util._

import scala.annotation.tailrec

object ManualHLintQuickfix {
  private val NotHaskellIdentifiers: Seq[IElementType] = Seq(HS_NEWLINE, HS_COMMENT, HS_NCOMMENT, TokenType.WHITE_SPACE, HS_HADDOCK, HS_NHADDOCK)

  def registerProblem(psiFile: PsiFile, virtualFile: VirtualFile, hlintInfo: HLintInfo, problemsHolder: ProblemsHolder, problemType: ProblemHighlightType, isOnTheFly: Boolean): Option[Unit] = {
    for {
      se <- findStartHaskellElement(virtualFile, psiFile, hlintInfo)
      () = ProgressManager.checkCanceled()
      ee <- findEndHaskellElement(virtualFile, psiFile, hlintInfo)
      () = ProgressManager.checkCanceled()
      sl <- fromOffset(virtualFile, se)
      () = ProgressManager.checkCanceled()
      el <- fromOffset(virtualFile, ee)
    } yield {
      ProgressManager.checkCanceled()

      hlintInfo.to match {
        case Some(to) if se.isValid && ee.isValid =>
          problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hlintInfo.hint, Array(createQuickfix(hlintInfo, se, ee, sl, el, to)), problemType, false, null, true, isOnTheFly))
        case None =>
          problemsHolder.registerProblem(new ProblemDescriptorBase(se, ee, hlintInfo.hint, Array(), problemType, false, null, true, isOnTheFly))
        case _ => ()
      }
    }
  }

  private def createQuickfix(hLintInfo: HLintInfo, startElement: PsiElement, endElement: PsiElement, startLineNumber: Int, endLineNumber: Int, to: String) = {
    new ManualHLintQuickfix(startElement, endElement, hLintInfo.startLine, hLintInfo.startColumn, removeLineBreaksAndExtraSpaces(startLineNumber, endLineNumber, to), hLintInfo.hint, hLintInfo.note)
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
    val element = offset.flatMap(offset => Option(psiFile.findElementAt(offset)))
    element.filterNot(e => ManualHLintQuickfix.NotHaskellIdentifiers.contains(e.getNode.getElementType))
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
      case Some(e) if ManualHLintQuickfix.NotHaskellIdentifiers.contains(e.getNode.getElementType) => findHaskellIdentifier(psiFile, offset - 1)
      case e => e
    }
  }
}

import scala.annotation.tailrec

class ManualHLintQuickfix(startElement: PsiElement, endElement: PsiElement, startLineNr: Int, startColumnNr: Int, toSuggestion: String, hint: String, note: Seq[String]) extends LocalQuickFixOnPsiElement(startElement, endElement) {
  override def getText: String = {
    if (toSuggestion.isEmpty) {
      "Remove"
    } else {
      s"$hint, change to `$toSuggestion`"
    } + noteText(note)
  }

  override def getFamilyName: String = "Inspection by HLint"

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    CommandProcessor.getInstance().executeCommand(project, () => {
      if (startElement == endElement) {
        applySuggestion(project, startElement)
      } else {
        val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement)
        for {
          se <- findDirectChildOfCommonParent(startElement, commonParent)
          ee <- findDirectChildOfCommonParent(endElement, commonParent)
        } yield {
          if (toSuggestion.isEmpty) {
            if (Option(ee.getNextSibling).exists(e => e.getNode.getElementType == HaskellTypes.HS_NEWLINE)) {
              commonParent.deleteChildRange(se, ee.getNextSibling)
            } else {
              commonParent.deleteChildRange(se, ee)
            }
          } else {
            Option(se.getNextSibling).foreach(ns => {
              commonParent.deleteChildRange(ns, ee)
              // Adding spaces in case of line break to get the indentation right for valid Haskell code (should eventually be solved by BNF which is indentation sensitive)
              applySuggestion(project, se)
            })
          }
        }
      }

      HaskellFileUtil.saveFile(psiFile)
      HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
    }, null, null)
  }

  private def applySuggestion(project: Project, startElement: PsiElement) = {
    HaskellElementFactory.createBody(project, toSuggestion.replaceAll("\n", "\n" + " " * (startColumnNr - 1))).foreach(startElement.replace)
  }

  @tailrec
  private def findDirectChildOfCommonParent(psiElement: PsiElement, parent: PsiElement): Option[PsiElement] = {
    Option(psiElement.getParent) match {
      case None => None
      case Some(p) if p == parent => Some(psiElement)
      case _ => findDirectChildOfCommonParent(psiElement.getParent, parent)
    }
  }

  private def noteText(note: Seq[String]) = {
    if (note.isEmpty) {
      ""
    } else {
      s" [Note: ${note.mkString("\n")}]"
    }
  }
}
