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

package com.powertuple.intellij.haskell.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.powertuple.intellij.haskell.psi.HaskellVar;
import org.jetbrains.annotations.NotNull;

public class HaskellStringLiteralManipulator extends AbstractElementManipulator<HaskellVar> {
    @Override
    public HaskellVar handleContentChange(HaskellVar psi, TextRange range, String newContent) {
        final String oldText = psi.getText();
        final String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
        return (HaskellVar) psi.setName(newText);
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(final HaskellVar element) {
        return getStringTokenRange(element);
    }

    public static TextRange getStringTokenRange(final HaskellVar element) {
        return TextRange.from(1, element.getTextLength() - 2);
    }
}
