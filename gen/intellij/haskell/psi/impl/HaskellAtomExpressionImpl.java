// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellAtomExpressionImpl extends HaskellExpressionImpl implements HaskellAtomExpression {

    public HaskellAtomExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitAtomExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellDotDot getDotDot() {
        return PsiTreeUtil.getChildOfType(this, HaskellDotDot.class);
    }

    @Override
    @Nullable
    public HaskellPragma getPragma() {
        return PsiTreeUtil.getChildOfType(this, HaskellPragma.class);
    }

    @Override
    @Nullable
    public HaskellQName getQName() {
        return PsiTreeUtil.getChildOfType(this, HaskellQName.class);
    }

    @Override
    @Nullable
    public HaskellQuasiQuote getQuasiQuote() {
        return PsiTreeUtil.getChildOfType(this, HaskellQuasiQuote.class);
    }

    @Override
    @Nullable
    public HaskellTextLiteral getTextLiteral() {
        return PsiTreeUtil.getChildOfType(this, HaskellTextLiteral.class);
    }

}
