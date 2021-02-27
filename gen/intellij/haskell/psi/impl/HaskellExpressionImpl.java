// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellExpressionImpl extends HaskellExpressionElementImpl implements HaskellExpression {

    public HaskellExpressionImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitExpression(this);
    }

    @Override
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
    public List<HaskellPragma> getPragmaList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
    }

    @Override
    @NotNull
    public List<HaskellQName> getQNameList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQName.class);
    }

    @Override
    @NotNull
    public List<HaskellQuasiQuote> getQuasiQuoteList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQuasiQuote.class);
    }

    @Override
    @NotNull
    public List<HaskellTextLiteral> getTextLiteralList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTextLiteral.class);
    }

}
