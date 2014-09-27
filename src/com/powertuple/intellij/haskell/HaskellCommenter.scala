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
package com.powertuple.intellij.haskell

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.tree.IElementType
import com.powertuple.intellij.haskell.psi.HaskellTypes

class HaskellCommenter extends CodeDocumentationAwareCommenter {
  def getLineCommentPrefix: String = {
    "-- "
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

  def getDocumentationCommentTokenType: IElementType = {
    HaskellTypes.HS_COMMENT
  }

  def getDocumentationCommentPrefix: String = {
    "-- | "
  }

  def getDocumentationCommentLinePrefix: String = {
    "-- | "
  }

  def getDocumentationCommentSuffix: String = {
    null
  }

  def isDocumentationComment(element: PsiComment): Boolean = {
    element.getText.startsWith(getDocumentationCommentLinePrefix)
  }
}