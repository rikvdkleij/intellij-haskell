// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.HaskellCfilesPragma;
import intellij.haskell.psi.HaskellImportDeclaration;
import intellij.haskell.psi.HaskellImportDeclarations;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellImportDeclarationsImpl extends HaskellCompositeElementImpl implements HaskellImportDeclarations {

  public HaskellImportDeclarationsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitImportDeclarations(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HaskellCfilesPragma> getCfilesPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellCfilesPragma.class);
  }

  @Override
  @NotNull
  public List<HaskellImportDeclaration> getImportDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellImportDeclaration.class);
  }

}
