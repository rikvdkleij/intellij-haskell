// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import intellij.haskell.psi.HaskellNamedElement;
import intellij.haskell.psi.HaskellVarid;
import intellij.haskell.psi.HaskellVisitor;
import intellij.haskell.psi.stubs.HaskellVaridStub;
import org.jetbrains.annotations.NotNull;

public class HaskellVaridImpl extends HaskellNamedStubBasedPsiElementBase<HaskellVaridStub> implements HaskellVarid {

    public HaskellVaridImpl(@NotNull HaskellVaridStub stub, IStubElementType type) {
        super(stub, type);
    }

    public HaskellVaridImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitVarid(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    public String getName() {
        return HaskellPsiImplUtil.getName(this);
    }

    public PsiElement setName(String newName) {
        return HaskellPsiImplUtil.setName(this, newName);
    }

    public HaskellNamedElement getNameIdentifier() {
        return HaskellPsiImplUtil.getNameIdentifier(this);
    }

    public PsiReference getReference() {
        return HaskellPsiImplUtil.getReference(this);
    }

    public ItemPresentation getPresentation() {
        return HaskellPsiImplUtil.getPresentation(this);
    }

}
