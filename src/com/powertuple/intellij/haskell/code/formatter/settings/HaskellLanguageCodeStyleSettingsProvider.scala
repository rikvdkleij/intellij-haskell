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

package com.powertuple.intellij.haskell.code.formatter.settings

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType
import com.intellij.psi.codeStyle.{CodeStyleSettingsCustomizable, CommonCodeStyleSettings, LanguageCodeStyleSettingsProvider}
import com.powertuple.intellij.haskell.HaskellLanguage
import org.jetbrains.annotations.NotNull

class HaskellLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  @NotNull
  def getLanguage: Language = {
    HaskellLanguage.Instance
  }

  override def getDefaultCommonSettings: CommonCodeStyleSettings = {
    val defaultSettings: CommonCodeStyleSettings = new CommonCodeStyleSettings(getLanguage)
        defaultSettings.KEEP_BLANK_LINES_IN_CODE = 1
    val indentOptions: CommonCodeStyleSettings.IndentOptions = defaultSettings.initIndentOptions
    indentOptions.INDENT_SIZE = 2
    indentOptions.CONTINUATION_INDENT_SIZE = 4
    indentOptions.TAB_SIZE = 2
    defaultSettings
  }

  override def customizeSettings(@NotNull consumer: CodeStyleSettingsCustomizable, @NotNull settingsType: LanguageCodeStyleSettingsProvider.SettingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions("SPACE_AFTER_COMMA", "SPACE_BEFORE_COMMA")
    }
  }

  override def getIndentOptionsEditor: SmartIndentOptionsEditor = {
    new SmartIndentOptionsEditor
  }

  private final val SpacingCodeSample =
    """l = [1,2,3]
      |g = (1,2,3)""".stripMargin

  override def getCodeSample(settingsType: SettingsType): String = {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      SpacingCodeSample
    } else {
      ""
    }
  }
}