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
  @Nullable
  public HaskellCcontext getCcontext() {
    return PsiTreeUtil.getChildOfType(this, HaskellCcontext.class);
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
  public HaskellCtypePragma getCtypePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellCtypePragma.class);
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
  public List<HaskellSimpletype> getSimpletypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSimpletype.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeSignature> getTypeSignatureList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignature.class);
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

  public Option<String> getModuleName() {
    return HaskellPsiImplUtil.getModuleName(this);
  }

  public HaskellNamedElement getDataTypeConstructor() {
    return HaskellPsiImplUtil.getDataTypeConstructor(this);
  }

}
