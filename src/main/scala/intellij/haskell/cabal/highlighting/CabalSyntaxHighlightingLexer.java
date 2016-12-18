package intellij.haskell.cabal.highlighting;

import com.intellij.lexer.FlexAdapter;

/**
 * Created by crobbins on 8/2/14.
 */
public class CabalSyntaxHighlightingLexer extends FlexAdapter {
    public CabalSyntaxHighlightingLexer() {
        super(new _CabalSyntaxHighlightingLexer());
    }
}
