/*
 * Copyright 2017 Rik van der Kleij
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
import com.intellij.psi.{PsiDocumentManager, PsiFile}
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellTypes

class EnterInHaddockHandler extends EnterHandlerDelegateAdapter {

  override def preprocessEnter(file: PsiFile, editor: Editor, caretOffset: Ref[Integer], caretAdvance: Ref[Integer], dataContext: DataContext, originalHandler: EditorActionHandler): Result = {
    if (!file.isInstanceOf[HaskellFile]) return Result.Continue

    val document = editor.getDocument
    PsiDocumentManager.getInstance(file.getProject).commitDocument(document)

    val result = Option(caretOffset.get()).flatMap(offset => Option(file.findElementAt(offset)).map(element => {
      if (element.getNode.getElementType == HaskellTypes.HS_HADDOCK) {
        document.insertString(offset, "-- ")
        caretAdvance.set(3)
        Result.Default
      } else {
        Result.Continue
      }
    }))
    result.getOrElse(Result.Continue)
  }
}
