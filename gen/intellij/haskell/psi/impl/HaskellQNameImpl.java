// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

public class HaskellQNameImpl extends HaskellCompositeElementImpl implements HaskellQName {

    public HaskellQNameImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitQName(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellQVarCon getQVarCon() {
        return PsiTreeUtil.getChildOfType(this, HaskellQVarCon.class);
    }

    @Override
    @Nullable
    public HaskellVarCon getVarCon() {
        return PsiTreeUtil.getChildOfType(this, HaskellVarCon.class);
    }

    @Override
    public String getName() {
        return HaskellPsiImplUtil.getName(this);
    }

    @Override
    public HaskellNamedElement getIdentifierElement() {
        return HaskellPsiImplUtil.getIdentifierElement(this);
    }

    @Override
    public Option<String> getQualifierName() {
        return HaskellPsiImplUtil.getQualifierName(this);
    }

}
