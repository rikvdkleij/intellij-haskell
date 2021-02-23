/*
 * Copyright 2014-2020 Rik van der Kleij
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
  override def getLanguage: Language = {
    HaskellLanguage.Instance
  }

  override def customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions): Unit = {
    indentOptions.INDENT_SIZE = 2
    indentOptions.CONTINUATION_INDENT_SIZE = 4
    indentOptions.TAB_SIZE = 2
    indentOptions.USE_TAB_CHARACTER = false
  }

  override def getIndentOptionsEditor: SmartIndentOptionsEditor = {
    new SmartIndentOptionsEditor(this)
  }

  override def getCodeSample(settingsType: SettingsType): String =
    """-- Reformatting is done externally by Ormolu.
      |-- Setting code style options here has no effect.
    """.stripMargin
}