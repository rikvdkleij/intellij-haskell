// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.collection.immutable.Seq;

import java.util.List;

public class HaskellDataDeclarationImpl extends HaskellTopDeclarationImpl implements HaskellDataDeclaration {

    public HaskellDataDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitDataDeclaration(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellCcontext getCcontext() {
        return PsiTreeUtil.getChildOfType(this, HaskellCcontext.class);
    }

    @Override
    @NotNull
    public List<HaskellConstr> getConstrList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConstr.class);
    }

    @Override
    @Nullable
    public HaskellDataDeclarationDeriving getDataDeclarationDeriving() {
        return PsiTreeUtil.getChildOfType(this, HaskellDataDeclarationDeriving.class);
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

    @Override
    @NotNull
    public List<HaskellTypeSignature> getTypeSignatureList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignature.class);
    }

    @Override
    public String getName() {
        return HaskellPsiImplUtil.getName(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        return HaskellPsiImplUtil.getPresentation(this);
    }

    @Override
    public Seq<HaskellNamedElement> getIdentifierElements() {
        return HaskellPsiImplUtil.getIdentifierElements(this);
    }

    @Override
    public Option<String> getModuleName() {
        return HaskellPsiImplUtil.getModuleName(this);
    }

    @Override
    public HaskellNamedElement getDataTypeConstructor() {
        return HaskellPsiImplUtil.getDataTypeConstructor(this);
    }

}
