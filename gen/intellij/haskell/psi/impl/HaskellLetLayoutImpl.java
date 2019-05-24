// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellExpression;
import intellij.haskell.psi.HaskellLetLayout;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellLetLayoutImpl extends HaskellCompositeElementImpl implements HaskellLetLayout {

    public HaskellLetLayoutImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitLetLayout(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellExpression> getExpressionList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellExpression.class);
    }

}
