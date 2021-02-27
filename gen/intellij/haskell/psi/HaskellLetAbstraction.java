// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HaskellLetAbstraction extends HaskellExpression {

    @NotNull
    List<HaskellCdecl> getCdeclList();

    @Nullable
    HaskellExpression getExpression();

}
