// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellExportImpl extends HaskellCompositeElementImpl implements HaskellExport {

    public HaskellExportImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitExport(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellDotDot getDotDot() {
        return PsiTreeUtil.getChildOfType(this, HaskellDotDot.class);
    }

    @Override
    @Nullable
    public HaskellModid getModid() {
        return PsiTreeUtil.getChildOfType(this, HaskellModid.class);
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

}
