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

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.command.undo.UndoUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.{HaskellElementFactory, HaskellTypes}
import intellij.haskell.util.OSUtil

import scala.annotation.tailrec

class HLintQuickfix(startElement: PsiElement, endElement: PsiElement, toSuggestion: String, note: Seq[String]) extends LocalQuickFixOnPsiElement(startElement, endElement) {
  override def getText: String = if (toSuggestion.isEmpty) {
    "Remove"
  } else {
    s"Change to: $toSuggestion ${formatNote(note)}"
  }

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
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
        Option(se.getNextSibling).foreach { ns =>
          commonParent.deleteChildRange(ns, ee)
          se.replace(HaskellElementFactory.createBody(project, toSuggestion))
        }
      }
    }
    UndoUtil.markPsiFileForUndo(file)
  }

  override def getFamilyName: String = "Inspection by HLint"

  @tailrec
  private def findDirectChildOfCommonParent(psiElement: PsiElement, parent: PsiElement): Option[PsiElement] = {
    Option(psiElement.getParent) match {
      case None => None
      case Some(p) if p == parent => Some(psiElement)
      case _ => findDirectChildOfCommonParent(psiElement.getParent, parent)
    }
  }

  private def formatNote(note: Seq[String]) = {
    val formattedNote = note.map(n => if (n.length > 1 && n.head == '"' && n.last == '"') n.substring(1, n.length - 1) else n).mkString(OSUtil.LineSeparator.toString)
    if (formattedNote.trim.isEmpty) {
      ""
    } else {
      "   -- " + formattedNote
    }
  }
}
