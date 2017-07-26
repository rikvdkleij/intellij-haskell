/*
 * Copyright 2014-2017 Rik van der Kleij
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

package intellij.haskell.editor.formatter.settings

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType
import com.intellij.psi.codeStyle.{CommonCodeStyleSettings, LanguageCodeStyleSettingsProvider}
import intellij.haskell.HaskellLanguage
import org.jetbrains.annotations.NotNull

class HaskellLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  @NotNull
  def getLanguage: Language = {
    HaskellLanguage.Instance
  }

  override def getDefaultCommonSettings: CommonCodeStyleSettings = {
    val defaultSettings = new CommonCodeStyleSettings(getLanguage)
    defaultSettings.KEEP_BLANK_LINES_IN_CODE = 1

    val indentOptions = defaultSettings.initIndentOptions
    indentOptions.INDENT_SIZE = 2
    indentOptions.CONTINUATION_INDENT_SIZE = 4
    indentOptions.TAB_SIZE = 2
    defaultSettings
  }

  override def getIndentOptionsEditor: SmartIndentOptionsEditor = {
    new SmartIndentOptionsEditor
  }

  override def getCodeSample(settingsType: SettingsType): String = ""
}