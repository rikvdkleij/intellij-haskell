package intellij.haskell;

import com.intellij.lexer.FlexAdapter;

public class HaskellLexer extends FlexAdapter {
    public HaskellLexer() {
        super(new _HaskellLexer());
    }
}
