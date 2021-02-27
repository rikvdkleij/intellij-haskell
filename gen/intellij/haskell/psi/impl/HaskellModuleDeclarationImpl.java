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

public class HaskellModuleDeclarationImpl extends HaskellTopDeclarationImpl implements HaskellModuleDeclaration {

    public HaskellModuleDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull HaskellVisitor visitor) {
        visitor.visitModuleDeclaration(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof HaskellVisitor) accept((HaskellVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public HaskellExports getExports() {
        return PsiTreeUtil.getChildOfType(this, HaskellExports.class);
    }

    @Override
    @NotNull
    public HaskellModid getModid() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellModid.class));
    }

    @Override
    @NotNull
    public List<HaskellPragma> getPragmaList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
    }

    @Override
    @NotNull
    public HaskellWhereClause getWhereClause() {
        return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellWhereClause.class));
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
