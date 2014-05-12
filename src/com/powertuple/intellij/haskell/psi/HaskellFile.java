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