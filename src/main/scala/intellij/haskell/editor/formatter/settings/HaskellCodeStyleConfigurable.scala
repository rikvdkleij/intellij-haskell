/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.application.options.{CodeStyleAbstractConfigurable, CodeStyleAbstractPanel, TabbedLanguageCodeStylePanel}
import com.intellij.psi.codeStyle.CodeStyleSettings
import intellij.haskell.HaskellLanguage
import org.jetbrains.annotations.NotNull

class HaskellCodeStyleConfigurable(@NotNull settings: CodeStyleSettings, cloneSettings: CodeStyleSettings) extends CodeStyleAbstractConfigurable(settings, cloneSettings, "Haskell") {

  protected def createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel = {
    new HaskellCodeStyleMainPanel(getCurrentSettings, settings)
  }

  override def getHelpTopic: String = {
    null
  }

  class HaskellCodeStyleMainPanel(currentSettings: CodeStyleSettings, settings: CodeStyleSettings) extends TabbedLanguageCodeStylePanel(HaskellLanguage.Instance, currentSettings, settings) {
    protected override def initTabs(settings: CodeStyleSettings) {
      addIndentOptionsTab(settings)
    }
  }
}