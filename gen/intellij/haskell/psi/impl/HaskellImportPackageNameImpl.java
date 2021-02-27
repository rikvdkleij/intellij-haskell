// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellImportPackageName;
import intellij.haskell.psi.HaskellTextLiteral;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

public class HaskellImportPackageNameImpl extends HaskellCompositeElementImpl implements HaskellImportPackageName {

    public HaskellImportPackageNameImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitImportPackageName(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellTextLiteral getTextLiteral() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellTextLiteral.class));
    }

}
