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

package com.powertuple.intellij.haskell.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.powertuple.intellij.haskell.HaskellFileType;
import com.powertuple.intellij.haskell.HaskellLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellFile extends PsiFileBase {
    public HaskellFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, HaskellLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return HaskellFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Haskell File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}