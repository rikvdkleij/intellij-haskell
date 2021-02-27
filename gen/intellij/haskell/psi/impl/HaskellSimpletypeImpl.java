// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.collection.immutable.Seq;

import java.util.List;

public class HaskellSimpletypeImpl extends HaskellCompositeElementImpl implements HaskellSimpletype {

    public HaskellSimpletypeImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitSimpletype(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
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
    @Nullable
    public HaskellTtype getTtype() {
        return PsiTreeUtil.getChildOfType(this, HaskellTtype.class);
    }

    @Override
    @NotNull
    public List<HaskellTypeSignature> getTypeSignatureList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignature.class);
    }

    @Override
    public Seq<HaskellNamedElement> getIdentifierElements() {
        return HaskellPsiImplUtil.getIdentifierElements(this);
    }

}
