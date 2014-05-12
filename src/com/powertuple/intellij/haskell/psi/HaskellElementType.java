package com.powertuple.intellij.haskell.psi;

import com.intellij.psi.tree.IElementType;
import com.powertuple.intellij.haskell.HaskellLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class HaskellElementType extends IElementType {
    public HaskellElementType(@NotNull @NonNls String debugName) {
        super(debugName, HaskellLanguage.INSTANCE);
    }
}