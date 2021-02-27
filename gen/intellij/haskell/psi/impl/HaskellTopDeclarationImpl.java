// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellTopDeclarationImpl extends HaskellCompositeElementImpl implements HaskellTopDeclaration {

    public HaskellTopDeclarationImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitTopDeclaration(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellExpression getExpression() {
        return PsiTreeUtil.getChildOfType(this, HaskellExpression.class);
    }

    @Override
    @Nullable
    public HaskellPragma getPragma() {
        return PsiTreeUtil.getChildOfType(this, HaskellPragma.class);
    }

    @Override
    @Nullable
    public HaskellTopDeclaration getTopDeclaration() {
        return PsiTreeUtil.getChildOfType(this, HaskellTopDeclaration.class);
    }

    @Override
    @Nullable
    public HaskellTypeSignature getTypeSignature() {
        return PsiTreeUtil.getChildOfType(this, HaskellTypeSignature.class);
    }

}
