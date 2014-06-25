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

package com.powertuple.intellij.haskell.highlighter;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.powertuple.intellij.haskell.HaskellParserDefinition;
import com.powertuple.intellij.haskell.psi.HaskellTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] PAIRS = new BracePair[]{
    new BracePair(HaskellTypes.HS_LEFT_PAREN, HaskellTypes.HS_RIGHT_PAREN, false),
    new BracePair(HaskellTypes.HS_LEFT_BRACE, HaskellTypes.HS_RIGHT_BRACE, false),
    new BracePair(HaskellTypes.HS_LEFT_BRACKET, HaskellTypes.HS_RIGHT_BRACKET, false),
    new BracePair(HaskellTypes.HS_IF, HaskellTypes.HS_ELSE, true),
  };

  @Override
  public BracePair[] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType type) {
    return TokenType.WHITE_SPACE == type
      || HaskellParserDefinition.COMMENTS.contains(type)
      || type == HaskellTypes.HS_COMMA
      || type == HaskellTypes.HS_RIGHT_PAREN
      || type == HaskellTypes.HS_RIGHT_BRACE
      || type == HaskellTypes.HS_RIGHT_BRACKET
      || null == type;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
