package intellij.haskell.cabal.lang.psi;

import com.intellij.psi.tree.IElementType;
import intellij.haskell.cabal.CabalLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CabalElementType extends IElementType {
    public CabalElementType(@NotNull @NonNls String debugName) {
        super(debugName, CabalLanguage.Instance);
    }
}
