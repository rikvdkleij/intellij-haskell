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
  public List<HaskellImportDeclaration> getImportDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellImportDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellPragma> getPragmaList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellPragma.class);
  }

}
