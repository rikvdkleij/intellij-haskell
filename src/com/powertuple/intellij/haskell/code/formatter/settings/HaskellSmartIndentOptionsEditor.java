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

package com.powertuple.intellij.haskell.code.formatter.settings;

import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellSmartIndentOptionsEditor extends SmartIndentOptionsEditor {

    private JCheckBox indentWhereWithTabSizeCheckBox;
    private JCheckBox indentDoWithTabSizeCheckBox;

    protected void addComponents() {
        super.addComponents();

        indentWhereWithTabSizeCheckBox = new JCheckBox("Indent 'where' with tab size");
        indentDoWithTabSizeCheckBox = new JCheckBox("Indent 'do' with tab size");
        add(indentWhereWithTabSizeCheckBox, true);
        add(indentDoWithTabSizeCheckBox, true);
    }

    public boolean isModified(final CodeStyleSettings settings, final CommonCodeStyleSettings.IndentOptions options) {
        boolean isModified = super.isModified(settings, options);

        isModified |= isFieldModified(indentWhereWithTabSizeCheckBox, getHaskellCodeStyleSettings(settings).INDENT_WHERE_WITH_TAB_SIZE);
        isModified |= isFieldModified(indentDoWithTabSizeCheckBox, getHaskellCodeStyleSettings(settings).INDENT_DO_WITH_TAB_SIZE);

        return isModified;
    }

    public void apply(final CodeStyleSettings settings, final CommonCodeStyleSettings.IndentOptions options) {
        super.apply(settings, options);

        getHaskellCodeStyleSettings(settings).INDENT_WHERE_WITH_TAB_SIZE = indentWhereWithTabSizeCheckBox.isSelected();
        getHaskellCodeStyleSettings(settings).INDENT_DO_WITH_TAB_SIZE = indentDoWithTabSizeCheckBox.isSelected();
    }

    public void reset(@NotNull final CodeStyleSettings settings, @NotNull final CommonCodeStyleSettings.IndentOptions options) {
        super.reset(settings, options);

        indentWhereWithTabSizeCheckBox.setSelected(getHaskellCodeStyleSettings(settings).INDENT_WHERE_WITH_TAB_SIZE);
        indentDoWithTabSizeCheckBox.setSelected(getHaskellCodeStyleSettings(settings).INDENT_DO_WITH_TAB_SIZE);
    }

    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        indentWhereWithTabSizeCheckBox.setEnabled(enabled);
        indentDoWithTabSizeCheckBox.setEnabled(enabled);
    }

    private HaskellCodeStyleSettings getHaskellCodeStyleSettings(final CodeStyleSettings settings) {
        return settings.getCustomSettings(HaskellCodeStyleSettings.class);
    }
}