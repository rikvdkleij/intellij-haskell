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

public class HaskellCideclsImpl extends HaskellCompositeElementImpl implements HaskellCidecls {

  public HaskellCideclsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitCidecls(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellDataDeclaration> getDataDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellDataDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellDefaultDeclaration> getDefaultDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellDefaultDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellExpression.class);
  }

  @Override
  @NotNull
  public List<HaskellInlinePragma> getInlinePragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellInlinePragma.class);
  }

  @Override
  @NotNull
  public List<HaskellInstanceDeclaration> getInstanceDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellInstanceDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellMinimalPragma> getMinimalPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellMinimalPragma.class);
  }

  @Override
  @NotNull
  public List<HaskellNewtypeDeclaration> getNewtypeDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellNewtypeDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellNoinlinePragma> getNoinlinePragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellNoinlinePragma.class);
  }

  @Override
  @NotNull
  public List<HaskellSpecializePragma> getSpecializePragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSpecializePragma.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeDeclaration> getTypeDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeSignature> getTypeSignatureList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignature.class);
  }

}
