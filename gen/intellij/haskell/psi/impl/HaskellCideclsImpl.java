// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
  public List<HaskellInlinelikePragma> getInlinelikePragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellInlinelikePragma.class);
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
  public List<HaskellSpecializePragma> getSpecializePragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellSpecializePragma.class);
  }

}
