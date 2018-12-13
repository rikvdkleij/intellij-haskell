// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellDotDot;
import intellij.haskell.psi.HaskellGeneralPragmaContent;
import intellij.haskell.psi.HaskellTextLiteral;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellGeneralPragmaContentImpl extends HaskellCompositeElementImpl implements HaskellGeneralPragmaContent {

    public HaskellGeneralPragmaContentImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitGeneralPragmaContent(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellDotDot> getDotDotList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellDotDot.class);
    }

    @Override
    @NotNull
    public List<HaskellTextLiteral> getTextLiteralList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTextLiteral.class);
    }

}
