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

package intellij.haskell.editor

import java.util

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import com.intellij.psi.{PsiComment, PsiElement, PsiWhiteSpace}
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class HaskellExtendWordSelectionHandler extends ExtendWordSelectionHandler {

  override def canSelect(e: PsiElement): Boolean = {
    Option(e.getContainingFile).exists(_.isInstanceOf[HaskellFile]) && !e.isInstanceOf[PsiComment] && !e.isInstanceOf[PsiWhiteSpace]
  }

  override def select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): util.List[TextRange] = {
    val startOffset = e.getTextRange.getStartOffset
    val nextEndOffsets = getOffsets(Some(e), ListBuffer.empty[(Int, IElementType)], (e: PsiElement) => e.getNextSibling, (e: PsiElement) => (e.getTextRange.getEndOffset, e.getNode.getElementType))

    val prevStartOffsets = getOffsets(Option(e.getPrevSibling), ListBuffer.empty[(Int, IElementType)], (e: PsiElement) => e.getPrevSibling, (e: PsiElement) => (e.getTextRange.getStartOffset, e.getNode.getElementType))
    val lastEndOffset = nextEndOffsets.lastOption.getOrElse((e.getTextRange.getEndOffset, e.getNode.getElementType))

    val allSelectOptions = nextEndOffsets.map(eo => (e.getNode.getElementType, eo._2, new TextRange(startOffset, eo._1))) ++ prevStartOffsets.map(so => (so._2, lastEndOffset._2, new TextRange(so._1, lastEndOffset._1)))

    allSelectOptions.filter(x =>
      if (x._1 == HS_LEFT_PAREN) {
        x._2 == HS_RIGHT_PAREN
      } else if (x._1 == HS_LEFT_BRACE) {
        x._2 == HS_RIGHT_BRACE
      } else if (x._1 == HS_LEFT_BRACKET) {
        x._2 == HS_RIGHT_BRACKET
      } else if (x._2 == HS_RIGHT_PAREN) {
        x._1 == HS_LEFT_PAREN
      } else if (x._2 == HS_RIGHT_BRACE) {
        x._1 == HS_LEFT_BRACE
      } else if (x._2 == HS_RIGHT_BRACKET) {
        x._1 == HS_LEFT_BRACKET
      } else {
        true
      }
    ).map(_._3).asJava
  }

  private def getOffsets(element: Option[PsiElement], offsets: ListBuffer[(Int, IElementType)], getSibling: PsiElement => PsiElement, getOffset: PsiElement => (Int, IElementType)): ListBuffer[(Int, IElementType)] = {
    def recur(e: PsiElement): ListBuffer[(Int, IElementType)] = {
      getOffsets(Option(getSibling(e)), offsets, getSibling, getOffset)
    }

    element match {
      case Some(e) =>
        HaskellPsiUtil.findQualifiedName(e) match {
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
