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

package com.powertuple.intellij.haskell.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import com.powertuple.intellij.haskell.HaskellLanguage;
import com.powertuple.intellij.haskell.HaskellParserDefinition;
import com.powertuple.intellij.haskell.formatter.settings.HaskellCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;

public class HaskellFormattingModelBuilder implements FormattingModelBuilder {

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        CommonCodeStyleSettings commonSettings = settings.getCommonSettings(HaskellLanguage.INSTANCE);
        HaskellCodeStyleSettings haskellSettings = settings.getCustomSettings(HaskellCodeStyleSettings.class);
        SpacingBuilder spacingBuilder = createSpacingBuilder(commonSettings, haskellSettings);
        HaskellFormattingBlock block = new HaskellFormattingBlock(element.getNode(), commonSettings, haskellSettings, spacingBuilder, Wrap.createWrap(WrapType.NONE, true), 0, 0, null);
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
    }

    private static SpacingBuilder createSpacingBuilder(CommonCodeStyleSettings settings, HaskellCodeStyleSettings haskellCodeStyleSettings) {
        return new SpacingBuilder(settings.getRootSettings(), HaskellLanguage.INSTANCE)
                .before(HS_COMMA).spaceIf(settings.SPACE_BEFORE_COMMA)
                .after(HS_COMMA).spaceIf(settings.SPACE_AFTER_COMMA)
                .before(HS_LEFT_PAREN).spaces(1)
                .after(HS_LEFT_PAREN).spaces(0)
                .before(HS_RIGHT_PAREN).spaces(0)
                .before(HS_LEFT_BRACKET).spaces(1)
                .after(HS_LEFT_BRACKET).spaces(0)
                .before(HS_RIGHT_BRACKET).spaces(0)
                .around(HaskellParserDefinition.RESERVED_IDS).spaces(1)
                .around(TokenSet.create(HS_COLON_COLON, HS_DOUBLE_RIGHT_ARROW, HS_EQUAL, HS_LEFT_ARROW, HS_RIGHT_ARROW)).spaces(1)
                .after(HS_VERTICAL_BAR).spaces(1)
                .after(HS_CON).spaces(1);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }
}