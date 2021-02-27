// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import intellij.haskell.psi.HaskellNamedElement;
import intellij.haskell.psi.HaskellVarsym;
import intellij.haskell.psi.HaskellVisitor;
import intellij.haskell.psi.stubs.HaskellVarsymStub;
import org.jetbrains.annotations.NotNull;

public class HaskellVarsymImpl extends HaskellNamedStubBasedPsiElementBase<HaskellVarsymStub> implements HaskellVarsym {

    public HaskellVarsymImpl(@NotNull HaskellVarsymStub stub, IStubElementType type) {
        super(stub, type);
    }

    public HaskellVarsymImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitVarsym(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public String getName() {
        return HaskellPsiImplUtil.getName(this);
    }

    @Override
    public PsiElement setName(String newName) {
        return HaskellPsiImplUtil.setName(this, newName);
    }

    @Override
    public HaskellNamedElement getNameIdentifier() {
        return HaskellPsiImplUtil.getNameIdentifier(this);
    }

    @Override
    public PsiReference getReference() {
        return HaskellPsiImplUtil.getReference(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        return HaskellPsiImplUtil.getPresentation(this);
    }

    @Override
    public String toString() {
        return HaskellPsiImplUtil.toString(this);
    }

}
