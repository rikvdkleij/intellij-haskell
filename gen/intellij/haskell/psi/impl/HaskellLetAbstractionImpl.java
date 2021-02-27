// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellCdecl;
import intellij.haskell.psi.HaskellExpression;
import intellij.haskell.psi.HaskellLetAbstraction;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellLetAbstractionImpl extends HaskellExpressionImpl implements HaskellLetAbstraction {

    public HaskellLetAbstractionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitLetAbstraction(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellCdecl> getCdeclList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCdecl.class);
    }

    @Override
    @Nullable
    public HaskellExpression getExpression() {
        return PsiTreeUtil.getChildOfType(this, HaskellExpression.class);
    }

}
