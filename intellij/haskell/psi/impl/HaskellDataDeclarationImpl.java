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
  @NotNull
  public List<HaskellExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellExpression.class);
  }

  @Override
  @Nullable
  public HaskellKindSignature getKindSignature() {
    return findChildByClass(HaskellKindSignature.class);
  }

  @Override
  @NotNull
  public List<HaskellQvar> getQvarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQvar.class);
  }

  @Override
  @NotNull
  public List<HaskellSimpletype> getSimpletypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSimpletype.class);
  }

}
