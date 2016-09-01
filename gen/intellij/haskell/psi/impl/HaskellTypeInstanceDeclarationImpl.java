// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import intellij.haskell.psi.HaskellExpression;
import intellij.haskell.psi.HaskellNamedElement;
import intellij.haskell.psi.HaskellTypeInstanceDeclaration;
import intellij.haskell.psi.HaskellVisitor;
import org.jetbrains.annotations.NotNull;
import scala.Option;
import scala.collection.Seq;

public class HaskellTypeInstanceDeclarationImpl extends HaskellCompositeElementImpl implements HaskellTypeInstanceDeclaration {

  public HaskellTypeInstanceDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTypeInstanceDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HaskellExpression getExpression() {
    return findNotNullChildByClass(HaskellExpression.class);
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

}
