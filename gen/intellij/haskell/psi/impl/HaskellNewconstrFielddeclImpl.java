// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellNewconstrFielddecl;
import intellij.haskell.psi.HaskellQName;
import intellij.haskell.psi.HaskellTypeSignature;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellNewconstrFielddeclImpl extends HaskellCompositeElementImpl implements HaskellNewconstrFielddecl {

    public HaskellNewconstrFielddeclImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitNewconstrFielddecl(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellQName getQName() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellQName.class));
    }

    @Override
    @NotNull
    public HaskellTypeSignature getTypeSignature() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellTypeSignature.class));
    }

}
