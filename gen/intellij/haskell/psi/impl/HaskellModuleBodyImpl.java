// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellModuleBody;
import intellij.haskell.psi.HaskellModuleDeclaration;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellModuleBodyImpl extends HaskellCompositeElementImpl implements HaskellModuleBody {

    public HaskellModuleBodyImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitModuleBody(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellModuleDeclaration getModuleDeclaration() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellModuleDeclaration.class));
    }

}
