/*
 * Copyright 2014-2018 Rik van der Kleij
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
import intellij.haskell.HaskellParserDefinition._

object HaskellBraceMatcher {

  import intellij.haskell.psi.HaskellTypes._

  private final val PAIRS = Array(
    new BracePair(HS_LEFT_PAREN, HS_RIGHT_PAREN, false),
    new BracePair(HS_PRAGMA_START, HS_PRAGMA_END, true),
    new BracePair(HS_LEFT_BRACE, HS_RIGHT_BRACE, true),
    new BracePair(HS_NCOMMENT_START, HS_NCOMMENT_END, true),
    new BracePair(HS_LEFT_BRACKET, HS_RIGHT_BRACKET, true)
  )
}

class HaskellBraceMatcher extends PairedBraceMatcher {
  def getPairs: Array[BracePair] = HaskellBraceMatcher.PAIRS

  def isPairedBracesAllowedBeforeType(lbraceType: IElementType, elementType: IElementType): Boolean = {
    !Ids.contains(elementType) && !Literals.contains(elementType)
  }

  def getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = {
    openingBraceOffset
  }
}
