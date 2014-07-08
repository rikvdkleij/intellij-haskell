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

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.powertuple.intellij.haskell.HaskellLanguage;
import org.jetbrains.annotations.NotNull;

public class HaskellLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @NotNull
    @Override
    public Language getLanguage() {
        return HaskellLanguage.INSTANCE;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            return SPACING_CODE_SAMPLE;
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            return DEFAULT_CODE_SAMPLE;
        } else if (settingsType == SettingsType.INDENT_SETTINGS) {
            return INDENT_CODE_SAMPLE;
        }
        return DEFAULT_CODE_SAMPLE;
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new HaskellSmartIndentOptionsEditor();
    }

    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 4;
        indentOptions.CONTINUATION_INDENT_SIZE = 8;
        indentOptions.TAB_SIZE = 2;

        return defaultSettings;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions(
                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA"
            );

//            consumer.showCustomOption(HaskellCodeStyleSettings.class, "SPACE_AROUND_OPERATORS", "Operator", CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS);
        }
//        else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
//            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
//        }
    }

    private static final String SPACING_CODE_SAMPLE =
            "l = [1,2,3]";

    private static final String INDENT_CODE_SAMPLE =
            "module SayHello\n" +
                    "(\n" +
                    "sayHello\n" +
                    ") where\n" +
                    "\n" +
                    "sayHello :: IO ()\n" +
                    "sayHello = do\n" +
                    "name <- getLine\n" +
                    "putStrLn $ greeting name\n" +
                    "where\n" +
                    "greeting name = \"Hello, \" ++ name ++ \"!\"";

    private static final String DEFAULT_CODE_SAMPLE =
            "module Quicksort where\n" +
                    "\n" +
                    "quicksort :: (Ord t) => [t] -> [t]\n" +
                    "quicksort [] = []\n" +
                    "quicksort (x:xs) = quicksort smallerOrEqual ++ [x] ++ quicksort larger\n" +
                    "where smallerOrEqual = [y | y <- xs, y <= x]\n" +
                    "larger = [y | y <- xs, y > x]\n" +
                    "\n" +
                    "result = quicksort [1,2]";
}