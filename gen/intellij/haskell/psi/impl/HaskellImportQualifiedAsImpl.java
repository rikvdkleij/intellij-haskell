// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellImportQualifiedAs;
import intellij.haskell.psi.HaskellQualifier;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellImportQualifiedAsImpl extends HaskellCompositeElementImpl implements HaskellImportQualifiedAs {

    public HaskellImportQualifiedAsImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitImportQualifiedAs(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellQualifier getQualifier() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellQualifier.class));
    }

}
