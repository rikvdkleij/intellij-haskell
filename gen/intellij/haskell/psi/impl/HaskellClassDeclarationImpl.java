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

public class HaskellClassDeclarationImpl extends HaskellTopDeclarationImpl implements HaskellClassDeclaration {

    public HaskellClassDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitClassDeclaration(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellCdecls getCdecls() {
        return PsiTreeUtil.getChildOfType(this, HaskellCdecls.class);
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
    public HaskellScontext getScontext() {
        return PsiTreeUtil.getChildOfType(this, HaskellScontext.class);
    }

    @Override
    @NotNull
    public List<HaskellTtype> getTtypeList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
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
