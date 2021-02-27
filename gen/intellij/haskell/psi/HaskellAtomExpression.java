// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.Nullable;

public interface HaskellAtomExpression extends HaskellExpression {

    @Nullable
    HaskellDotDot getDotDot();

    @Nullable
    HaskellPragma getPragma();

    @Nullable
    HaskellQName getQName();

    @Nullable
    HaskellQuasiQuote getQuasiQuote();

    @Nullable
    HaskellTextLiteral getTextLiteral();

}
