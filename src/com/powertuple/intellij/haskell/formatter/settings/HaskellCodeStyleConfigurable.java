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

package com.powertuple.intellij.haskell.formatter.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.powertuple.intellij.haskell.HaskellLanguage;
import org.jetbrains.annotations.NotNull;

public class HaskellCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
    public HaskellCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
        super(settings, cloneSettings, "Haskell");
    }

    @Override
    protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
        return new HaskellCodeStyleMainPanel(getCurrentSettings(), settings);
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    private static class HaskellCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        private HaskellCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(HaskellLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
