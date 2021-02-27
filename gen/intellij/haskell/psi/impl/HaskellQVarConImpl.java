// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellQVarConImpl extends HaskellCompositeElementImpl implements HaskellQVarCon {

    public HaskellQVarConImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitQVarCon(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellConsym getConsym() {
        return PsiTreeUtil.getChildOfType(this, HaskellConsym.class);
    }

    @Override
    @Nullable
    public HaskellQCon getQCon() {
        return PsiTreeUtil.getChildOfType(this, HaskellQCon.class);
    }

    @Override
    @Nullable
    public HaskellQualifier getQualifier() {
        return PsiTreeUtil.getChildOfType(this, HaskellQualifier.class);
    }

    @Override
    @Nullable
    public HaskellVarid getVarid() {
        return PsiTreeUtil.getChildOfType(this, HaskellVarid.class);
    }

    @Override
    @Nullable
    public HaskellVarsym getVarsym() {
        return PsiTreeUtil.getChildOfType(this, HaskellVarsym.class);
    }

    @Override
    public String getName() {
        return HaskellPsiImplUtil.getName(this);
    }

    @Override
    public HaskellNamedElement getIdentifierElement() {
        return HaskellPsiImplUtil.getIdentifierElement(this);
    }

}
