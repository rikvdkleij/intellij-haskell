package com.powertuple.intellij.haskell;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

public class HaskellParserUtil extends GeneratedParserUtilBase {

    public static boolean ghcMod(PsiBuilder builder_, int level_) {
//        if (builder_.eof()) return true;
        System.out.println("test rik van rikkie");
//        IElementType one = builder_.rawLookup(1);
//        IElementType two = builder_.rawLookup(2);
//        if (one == TokenType.WHITE_SPACE && (two == HaskellTypes.HS_DOT || two == null) || one == null && builder_.getTokenType() == HaskellTypes.HS_DOT) {
//            builder_.remapCurrentToken(TokenType.ERROR_ELEMENT);
//            return true;
//        }
        return false;
    }
}
