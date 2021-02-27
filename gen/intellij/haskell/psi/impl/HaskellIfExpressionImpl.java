// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellExpression;
import intellij.haskell.psi.HaskellIfExpression;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellIfExpressionImpl extends HaskellExpressionImpl implements HaskellIfExpression {

    public HaskellIfExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitIfExpression(this);
    }

    @Override
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
