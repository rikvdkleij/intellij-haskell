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
import scala.collection.Seq;

import java.util.List;

public class HaskellInstanceDeclarationImpl extends HaskellCompositeElementImpl implements HaskellInstanceDeclaration {

    public HaskellInstanceDeclarationImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitInstanceDeclaration(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellCidecls getCidecls() {
        return PsiTreeUtil.getChildOfType(this, HaskellCidecls.class);
    }

    @Override
    @Nullable
    public HaskellInst getInst() {
        return PsiTreeUtil.getChildOfType(this, HaskellInst.class);
    }

    @Override
    @Nullable
    public HaskellPragma getPragma() {
        return PsiTreeUtil.getChildOfType(this, HaskellPragma.class);
    }

    @Override
    @Nullable
    public HaskellQName getQName() {
        return PsiTreeUtil.getChildOfType(this, HaskellQName.class);
    }

    @Override
    @Nullable
    public HaskellScontext getScontext() {
        return PsiTreeUtil.getChildOfType(this, HaskellScontext.class);
    }

    @Override
    @Nullable
    public HaskellTypeEquality getTypeEquality() {
        return PsiTreeUtil.getChildOfType(this, HaskellTypeEquality.class);
    }

    @Override
    @NotNull
    public List<HaskellVarCon> getVarConList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellVarCon.class);
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

}
