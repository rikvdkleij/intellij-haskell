// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import intellij.haskell.psi.HaskellConsym;
import intellij.haskell.psi.HaskellNamedElement;
import intellij.haskell.psi.HaskellVisitor;
import intellij.haskell.psi.stubs.HaskellConsymStub;
import org.jetbrains.annotations.NotNull;

public class HaskellConsymImpl extends HaskellNamedStubBasedPsiElementBase<HaskellConsymStub> implements HaskellConsym {

    public HaskellConsymImpl(@NotNull HaskellConsymStub stub, IStubElementType type) {
        super(stub, type);
    }

    public HaskellConsymImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitConsym(this);
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

}
