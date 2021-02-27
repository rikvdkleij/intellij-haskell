// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellTypeFamilyTypeImpl extends HaskellCompositeElementImpl implements HaskellTypeFamilyType {

    public HaskellTypeFamilyTypeImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitTypeFamilyType(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellCcontext> getCcontextList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCcontext.class);
    }

    @Override
    @NotNull
    public List<HaskellPragma> getPragmaList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
    }

    @Override
    @NotNull
    public List<HaskellQName> getQNameList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQName.class);
    }

    @Override
    @NotNull
    public List<HaskellQNames> getQNamesList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQNames.class);
    }

    @Override
    @NotNull
    public List<HaskellTtype> getTtypeList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
    }

}
