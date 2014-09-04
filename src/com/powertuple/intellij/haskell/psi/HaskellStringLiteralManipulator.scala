/*
 * Copyright 2014 Rik van der Kleij

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
package com.powertuple.intellij.haskell.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.jetbrains.annotations.NotNull

object HaskellStringLiteralManipulator {
  def getStringTokenRange(element: HaskellQvar): TextRange = {
    TextRange.from(1, element.getTextLength - 2)
  }
}

class HaskellStringLiteralManipulator extends AbstractElementManipulator[HaskellQvar] {
  def handleContentChange(psi: HaskellQvar, range: TextRange, newContent: String): HaskellQvar = {
    val oldText: String = psi.getText
    val newText: String = oldText.substring(0, range.getStartOffset) + newContent + oldText.substring(range.getEndOffset)
    psi.setName(newText).asInstanceOf[HaskellQvar]
  }

  @NotNull
  override def getRangeInElement(element: HaskellQvar): TextRange = {
    HaskellStringLiteralManipulator.getStringTokenRange(element)
  }
}