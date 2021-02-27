// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HaskellCdeclDataDeclarationImpl extends HaskellTopDeclarationImpl implements HaskellCdeclDataDeclaration {

    public HaskellCdeclDataDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitCdeclDataDeclaration(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<HaskellKindSignature> getKindSignatureList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellKindSignature.class);
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
    public HaskellSimpletype getSimpletype() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellSimpletype.class));
    }

    @Override
    @Nullable
    public HaskellTtype getTtype() {
        return PsiTreeUtil.getChildOfType(this, HaskellTtype.class);
    }

}
