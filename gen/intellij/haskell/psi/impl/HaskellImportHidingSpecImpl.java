// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellImportHidingSpecImpl extends HaskellCompositeElementImpl implements HaskellImportHidingSpec {

    public HaskellImportHidingSpecImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitImportHidingSpec(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public HaskellImportHiding getImportHiding() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellImportHiding.class));
    }

    @Override
    @NotNull
    public List<HaskellImportId> getImportIdList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellImportId.class);
    }

    @Override
    @NotNull
    public List<HaskellPragma> getPragmaList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
    }

}
