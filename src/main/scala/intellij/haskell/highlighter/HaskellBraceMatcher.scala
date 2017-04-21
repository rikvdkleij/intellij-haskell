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

package intellij.haskell.highlighter

import com.intellij.lang.{BracePair, PairedBraceMatcher}
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import intellij.haskell.psi.HaskellTypes

object HaskellBraceMatcher {
  private final val PAIRS = Array(
    new BracePair(HaskellTypes.HS_LEFT_PAREN, HaskellTypes.HS_RIGHT_PAREN, true),
    new BracePair(HaskellTypes.HS_PRAGMA_START, HaskellTypes.HS_PRAGMA_END, true),
    new BracePair(HaskellTypes.HS_LEFT_BRACE, HaskellTypes.HS_RIGHT_BRACE, true),
    new BracePair(HaskellTypes.HS_BACKQUOTE, HaskellTypes.HS_BACKQUOTE, false),
    new BracePair(HaskellTypes.HS_NCOMMENT_START, HaskellTypes.HS_NCOMMENT_END, true),
    new BracePair(HaskellTypes.HS_LEFT_BRACKET, HaskellTypes.HS_RIGHT_BRACKET, true))
}

class HaskellBraceMatcher extends PairedBraceMatcher {
  def getPairs: Array[BracePair] = HaskellBraceMatcher.PAIRS

  def isPairedBracesAllowedBeforeType(lbraceType: IElementType, elementType: IElementType): Boolean = {
    elementType != HaskellTypes.HS_VAR_ID && elementType != HaskellTypes.HS_CON_ID && elementType != HaskellTypes.HS_VARSYM_ID && elementType != HaskellTypes.HS_CONSYM_ID
  }

  def getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = {
    openingBraceOffset
  }
}
