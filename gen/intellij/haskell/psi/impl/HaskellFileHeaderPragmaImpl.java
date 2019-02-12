// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellFileHeaderPragma;
import intellij.haskell.psi.HaskellGeneralPragmaContent;
import intellij.haskell.psi.HaskellOptionsGhcPragma;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellFileHeaderPragmaImpl extends HaskellCompositeElementImpl implements HaskellFileHeaderPragma {

    public HaskellFileHeaderPragmaImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitFileHeaderPragma(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellGeneralPragmaContent> getGeneralPragmaContentList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellGeneralPragmaContent.class);
    }

    @Override
    @Nullable
    public HaskellOptionsGhcPragma getOptionsGhcPragma() {
        return PsiTreeUtil.getChildOfType(this, HaskellOptionsGhcPragma.class);
    }

}
