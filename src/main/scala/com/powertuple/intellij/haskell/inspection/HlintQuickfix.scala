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

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.command.undo.UndoUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.psi.{HaskellElementFactory, HaskellTypes}
import com.powertuple.intellij.haskell.util.OSUtil

import scala.annotation.tailrec

class HlintQuickfix(startElement: PsiElement, endElement: PsiElement, toSuggestion: String, note: Seq[String]) extends LocalQuickFixOnPsiElement(startElement, endElement) {
  override def getText: String = if (toSuggestion.isEmpty) {
    "Remove"
  } else {
    s"Change to: $toSuggestion ${formatNote(note)}"
  }

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    val parent = PsiTreeUtil.findCommonParent(startElement, endElement)
    for {
      se <- findParentBelowParent(startElement, parent)
      ee <- findParentBelowParent(endElement, parent)
    } yield {
      if (toSuggestion.isEmpty) {
        if (Option(ee.getNextSibling).exists(e => e.getNode.getElementType == HaskellTypes.HS_NEWLINE || e.getNode.getElementType == HaskellTypes.HS_SNL)) {
          parent.deleteChildRange(se, ee.getNextSibling)
        } else {
          parent.deleteChildRange(se, ee)
        }
      } else {
        if (se.getChildren.headOption.exists(_.getText != startElement.getText)) {
          // In this case se is parent of startElement (so line expression), for example when startElement is in different line expression
          // than line expression of endElement. Common parent is in this case complete expression itself.

          // Delete line expression after line expression of start element
          parent.deleteChildRange(findNextHaskellElement(se), ee)

          // Delete elements after start element is same line expression
          val startElementParent = startElement.getParent
          Option(startElement.getNextSibling).map(next => startElementParent.deleteChildRange(next, startElementParent.getLastChild))

          // Add new line with tab and replace start element by HLint suggestion
          startElementParent.addBefore(HaskellElementFactory.createNewLine(project), startElement)
          startElementParent.addBefore(HaskellElementFactory.createTab(project), startElement)
          startElement.replace(HaskellElementFactory.createBody(project, toSuggestion))
        } else {
          parent.deleteChildRange(findParentBelowParent(se.getNextSibling, parent).getOrElse(se), ee)
          if (se.getNode.getElementType == HaskellTypes.HS_LEFT_PAREN) {
            parent.addBefore(HaskellElementFactory.createWhiteSpace(project), se)
          }
          se.replace(HaskellElementFactory.createBody(project, toSuggestion))
        }
      }
    }
    UndoUtil.markPsiFileForUndo(file)
  }

  override def getFamilyName: String = "Inspection by HLint"

  @tailrec
  private def findParentBelowParent(psiElement: PsiElement, parent: PsiElement): Option[PsiElement] = {
    Option(psiElement.getParent) match {
      case None => None
      case Some(p) if p == parent => Some(psiElement)
      case _ => findParentBelowParent(psiElement.getParent, parent)
    }
  }

  private def formatNote(note: Seq[String]) = {
    val formattedNote = note.map(n => if (n.size > 1 && n.head == '"' && n.last == '"') n.substring(1, n.size - 1) else n).mkString(OSUtil.LineSeparator.toString)
    if (formattedNote.trim.isEmpty) {
      ""
    } else {
      "   -- " + formattedNote
    }
  }

  private def findNextHaskellElement(element: PsiElement): PsiElement = {
    val next = element.getNextSibling
    if (HlintInspectionTool.NotHaskellIdentifiers.contains(next.getNode.getElementType)) {
      findNextHaskellElement(next)
    } else {
      next
    }
  }
}
