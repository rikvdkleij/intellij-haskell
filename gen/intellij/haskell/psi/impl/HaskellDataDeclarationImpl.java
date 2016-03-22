// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static intellij.haskell.psi.HaskellTypes.*;
import intellij.haskell.psi.*;
import com.intellij.navigation.ItemPresentation;
import scala.collection.Seq;

public class HaskellDataDeclarationImpl extends HaskellCompositeElementImpl implements HaskellDataDeclaration {

  public HaskellDataDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitDataDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellCdecl> getCdeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCdecl.class);
  }

  @Override
  @NotNull
  public List<HaskellConstr1> getConstr1List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConstr1.class);
  }

  @Override
  @NotNull
  public List<HaskellConstr2> getConstr2List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConstr2.class);
  }

  @Override
  @NotNull
  public List<HaskellConstr3> getConstr3List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConstr3.class);
  }

  @Override
  @NotNull
  public List<HaskellConstr4> getConstr4List() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellConstr4.class);
  }

  @Override
  @Nullable
  public HaskellContext getContext() {
    return findChildByClass(HaskellContext.class);
  }

  @Override
  @Nullable
  public HaskellCtypePragma getCtypePragma() {
    return findChildByClass(HaskellCtypePragma.class);
  }

  @Override
  @Nullable
  public HaskellDataDeclarationDeriving getDataDeclarationDeriving() {
    return findChildByClass(HaskellDataDeclarationDeriving.class);
  }

  @Override
  @Nullable
  public HaskellExpression getExpression() {
    return findChildByClass(HaskellExpression.class);
  }

  @Override
  @NotNull
  public HaskellSimpletype getSimpletype() {
    return findNotNullChildByClass(HaskellSimpletype.class);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  public Seq<HaskellNamedElement> getIdentifierElements() {
    return HaskellPsiImplUtil.getIdentifierElements(this);
  }

  public HaskellNamedElement getSimpleType() {
    return HaskellPsiImplUtil.getSimpleType(this);
  }

}
