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

package intellij.haskell.editor

import java.util

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.{PsiComment, PsiElement, PsiWhiteSpace}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class HaskellExtendWordSelectioner extends ExtendWordSelectionHandler {
  override def canSelect(e: PsiElement): Boolean = {
    !(e.isInstanceOf[PsiComment] || e.isInstanceOf[PsiWhiteSpace])
  }

  override def select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): util.List[TextRange] = {
    val startOffset = e.getTextRange.getStartOffset
    val nextEndOffsets = getOffsets(Some(e), ListBuffer.empty[Int], (e: PsiElement) => e.getNextSibling, (e: PsiElement) => e.getTextRange.getEndOffset)

    val prevStartOffsets = getOffsets(HaskellPsiUtil.findQualifiedNameElement(e).flatMap(qe => Option(qe.getPrevSibling)), ListBuffer.empty[Int], (e: PsiElement) => e.getPrevSibling, (e: PsiElement) => e.getTextRange.getStartOffset)
    val lastEndOffset = nextEndOffsets.lastOption.getOrElse(e.getTextRange.getEndOffset)

    (nextEndOffsets.map(eo => new TextRange(startOffset, eo)) ++ prevStartOffsets.map(so => new TextRange(so, lastEndOffset))).asJava
  }

  private def getOffsets(element: Option[PsiElement], offsets: ListBuffer[Int], getSibling: PsiElement => PsiElement, getOffset: PsiElement => Int): ListBuffer[Int] = {
    def recur(e: PsiElement): ListBuffer[Int] = {
      getOffsets(Option(getSibling(e)), offsets, getSibling, getOffset)
    }

    element match {
      case Some(e) =>
        HaskellPsiUtil.findQualifiedNameElement(e) match {
          case None => e match {
            case e: PsiWhiteSpace => recur(e)
            case e: PsiElement if e.getNode.getElementType == HS_COMMA => recur(e)
            case e: PsiElement if e.getNode.getElementType == HS_NEWLINE | e.getNode.getElementType == HS_EQUAL | e.getNode.getElementType == HS_LEFT_ARROW => offsets
            case _ =>
              offsets += getOffset(e)
              recur(e)
          }
          case Some(qe) =>
            offsets += getOffset(qe)
            recur(qe)
        }
      case None => offsets
    }
  }
}
