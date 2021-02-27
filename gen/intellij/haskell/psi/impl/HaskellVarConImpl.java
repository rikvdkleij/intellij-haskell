// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HaskellVarConImpl extends HaskellCompositeElementImpl implements HaskellVarCon {

    public HaskellVarConImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitVarCon(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellConid getConid() {
        return PsiTreeUtil.getChildOfType(this, HaskellConid.class);
    }

    @Override
    @Nullable
    public HaskellConsym getConsym() {
        return PsiTreeUtil.getChildOfType(this, HaskellConsym.class);
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
