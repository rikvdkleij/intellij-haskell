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

public class HaskellModuleBodyImpl extends HaskellCompositeElementImpl implements HaskellModuleBody {

  public HaskellModuleBodyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitModuleBody(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellImportDeclarations getImportDeclarations() {
    return PsiTreeUtil.getChildOfType(this, HaskellImportDeclarations.class);
  }

  @Override
  @Nullable
  public HaskellModuleDeclaration getModuleDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellModuleDeclaration.class);
  }

  @Override
  @NotNull
  public List<HaskellTopDeclaration> getTopDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTopDeclaration.class);
  }

}
