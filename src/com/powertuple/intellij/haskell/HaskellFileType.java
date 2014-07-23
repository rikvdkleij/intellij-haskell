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

package com.powertuple.intellij.haskell;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HaskellFileType extends LanguageFileType {
    public static final HaskellFileType INSTANCE = new HaskellFileType();

    public static HaskellFileType MODULE = new HaskellFileType();

    private HaskellFileType() {
        super(com.powertuple.intellij.haskell.HaskellLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Haskell file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Haskell language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "hs";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return HaskellIcons.HASKELL_SMALL_LOGO;
    }
}
