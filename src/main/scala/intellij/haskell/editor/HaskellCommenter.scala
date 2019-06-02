/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.tree.IElementType
import intellij.haskell.psi.HaskellTypes

class HaskellCommenter extends CodeDocumentationAwareCommenter {
  def getLineCommentPrefix: String = {
    "--"
  }

  def getBlockCommentPrefix: String = {
    "{-"
  }

  def getBlockCommentSuffix: String = {
    "-}"
  }

  def getCommentedBlockCommentPrefix: String = {
    "{-"
  }

  def getCommentedBlockCommentSuffix: String = {
    "-}"
  }

  def getLineCommentTokenType: IElementType = {
    HaskellTypes.HS_COMMENT
  }

  def getBlockCommentTokenType: IElementType = {
    HaskellTypes.HS_NCOMMENT
  }

  // Haskell documentation does not have similar syntax/structure as Javadoc so makes no sense to put some values here.
  def getDocumentationCommentTokenType: IElementType = {
    null
  }

  def getDocumentationCommentPrefix: String = {
    null
  }

  def getDocumentationCommentLinePrefix: String = {
    null
  }

  def getDocumentationCommentSuffix: String = {
    null
  }

  def isDocumentationComment(element: PsiComment): Boolean = {
    element.getText.startsWith("-- |") || element.getText.startsWith("{-|")
  }
}