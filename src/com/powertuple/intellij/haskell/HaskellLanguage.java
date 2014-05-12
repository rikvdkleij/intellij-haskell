package com.powertuple.intellij.haskell;

import com.intellij.lang.Language;

public class HaskellLanguage extends Language {
    public static final HaskellLanguage INSTANCE = new HaskellLanguage();

    public HaskellLanguage() {
        super("Haskell");
    }

    @Override
    public String getDisplayName() {
        return "Haskell language";
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
