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

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.{PsiDocumentManager, PsiElement, PsiFile, TokenType}
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._

class IndentAfterEnterHandler extends EnterHandlerDelegateAdapter {

  private val IndentTokenSet = TokenSet.create(HS_WHERE, HS_OF, HS_EQUAL, HS_IN, HS_DO, HS_IF, HS_THEN, HS_ELSE)

  override def preprocessEnter(file: PsiFile, editor: Editor, caretOffset: Ref[Integer], caretAdvance: Ref[Integer], dataContext: DataContext, originalHandler: EditorActionHandler): Result = {
    if (!file.isInstanceOf[HaskellFile] &&
      Option(caretOffset.get()).flatMap(offset => Option(file.findElementAt(offset))).exists(e => HaskellPsiUtil.findExpression(e).isEmpty)) return Result.Continue

    val document = editor.getDocument
    PsiDocumentManager.getInstance(file.getProject).commitDocument(document)

    val result = Option(caretOffset.get()).flatMap(offset => findNonWhiteSpaceElement(file, offset - 1).orElse(findNonWhiteSpaceElement(file, offset - 2)).map(element => {
      if (IndentTokenSet.contains(element.getNode.getElementType)) {
        document.insertString(offset, "  ")
        caretAdvance.set(2)
        Result.Default
      } else {
        Result.Continue
      }
    })
    )
    result.getOrElse(Result.Continue)
  }


  private def findNonWhiteSpaceElement(file: PsiFile, offset: Int): Option[PsiElement] = {
    Option(file.findElementAt(offset - 1)).filterNot(_.getNode.getElementType == TokenType.WHITE_SPACE)
  }
}
