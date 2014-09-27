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

package com.powertuple.intellij.haskell.highlighter

import com.intellij.lang.{BracePair, PairedBraceMatcher}
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.powertuple.intellij.haskell.psi.HaskellTypes

object HaskellBraceMatcher {
  private final val PAIRS = Array(
    new BracePair(HaskellTypes.HS_LEFT_PAREN, HaskellTypes.HS_RIGHT_PAREN, false),
    new BracePair(HaskellTypes.HS_LEFT_BRACKET, HaskellTypes.HS_RIGHT_BRACKET, false))
}

class HaskellBraceMatcher extends PairedBraceMatcher {
  def getPairs: Array[BracePair] = HaskellBraceMatcher.PAIRS

  def isPairedBracesAllowedBeforeType(lbraceType: IElementType, elementType: IElementType): Boolean = true

  def getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = {
    openingBraceOffset
  }
}