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

package com.powertuple.intellij.haskell;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import com.powertuple.intellij.haskell.psi.HaskellTypes;

public class HaskellCommenter implements CodeDocumentationAwareCommenter {
    public String getLineCommentPrefix() {
        return "-- ";
    }

    public String getBlockCommentPrefix() {
        return "{-\n";
    }

    public String getBlockCommentSuffix() {
        return "\n-}";
    }

    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Override
    public IElementType getLineCommentTokenType() {
        return HaskellTypes.HS_COMMENT;
    }

    @Override
    public IElementType getBlockCommentTokenType() {
        return HaskellTypes.HS_NCOMMENT;
    }

    @Override
    public IElementType getDocumentationCommentTokenType() {
        return HaskellTypes.HS_COMMENT;
    }

    @Override
    public String getDocumentationCommentPrefix() {
        return null;
    }

    @Override
    public String getDocumentationCommentLinePrefix() {
        return "-- | ";
    }

    @Override
    public String getDocumentationCommentSuffix() {
        return null;
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        return element.getText().startsWith(getDocumentationCommentLinePrefix());
    }
}
