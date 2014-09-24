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

package com.powertuple.intellij.haskell.code.formatter

import com.intellij.formatting._
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.{CodeStyleSettings, CommonCodeStyleSettings}
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.code.formatter.settings.HaskellCodeStyleSettings
import com.powertuple.intellij.haskell.psi.HaskellTypes._
import com.powertuple.intellij.haskell.{HaskellLanguage, HaskellParserDefinition}
import org.jetbrains.annotations.{NotNull, Nullable}

object HaskellFormattingModelBuilder {
  def createSpacingBuilder(settings: CommonCodeStyleSettings, haskellCodeStyleSettings: HaskellCodeStyleSettings): SpacingBuilder = {
    new SpacingBuilder(settings.getRootSettings, HaskellLanguage.Instance).
        beforeInside(HS_COMMA, HS_LINE_EXPRESSION).spaceIf(settings.SPACE_BEFORE_COMMA).
        afterInside(HS_COMMA, HS_LINE_EXPRESSION).spaceIf(settings.SPACE_AFTER_COMMA).
        before(HS_LEFT_PAREN).spaces(1).
        after(HS_LEFT_PAREN).spaces(0).
        before(HS_RIGHT_PAREN).spaces(0).
        after(HS_RIGHT_PAREN).spaces(1).
        before(HS_LEFT_BRACKET).spaces(1).
        after(HS_LEFT_BRACKET).spaces(0).
        before(HS_RIGHT_BRACKET).spaces(0).
        after(HS_RIGHT_BRACKET).spaces(1).
        after(HS_RIGHT_BRACE).spaces(1).
        before(HS_LEFT_BRACE).spaces(1).
        after(HS_LEFT_BRACE).spaces(0).
        before(HS_RIGHT_BRACE).spaces(0).
        after(HS_RIGHT_BRACE).spaces(1).
        around(HaskellParserDefinition.RESERVED_IDS).spaces(1).
        around(TokenSet.create(HS_TTYPE)).spaces(1).
        after(HS_VARS).spaces(1).
        after(HS_QVAR).spaces(1).
        before(HS_QVAROP).spaces(1).
        after(HS_QCON).spaces(1).
        after(HS_QCONOP).spaces(1).
        after(HS_VERTICAL_BAR).spaces(1).
        around(TokenSet.create(HS_COLON_COLON, HS_DOUBLE_RIGHT_ARROW, HS_EQUAL, HS_LEFT_ARROW, HS_RIGHT_ARROW)).spaces(1)
  }
}

class HaskellFormattingModelBuilder extends FormattingModelBuilder {

  @NotNull
  def createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel = {
    val commonSettings: CommonCodeStyleSettings = settings.getCommonSettings(HaskellLanguage.Instance)
    val haskellSettings: HaskellCodeStyleSettings = settings.getCustomSettings(classOf[HaskellCodeStyleSettings])
    val spacingBuilder: SpacingBuilder = HaskellFormattingModelBuilder.createSpacingBuilder(commonSettings, haskellSettings)
    val block: HaskellFormattingBlock = new HaskellFormattingBlock(element.getNode, None, spacingBuilder, null)
    FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile, block, settings)
  }

  @Nullable
  def getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange = {
    null
  }
}