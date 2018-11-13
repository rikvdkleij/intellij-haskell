// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellTopDeclaration;
import intellij.haskell.psi.HaskellTopDeclarationLine;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellTopDeclarationLineImpl extends HaskellCompositeElementImpl implements HaskellTopDeclarationLine {

    public HaskellTopDeclarationLineImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitTopDeclarationLine(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellTopDeclaration getTopDeclaration() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellTopDeclaration.class));
    }

}
