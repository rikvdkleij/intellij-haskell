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

public class HaskellInstanceDeclarationImpl extends HaskellCompositeElementImpl implements HaskellInstanceDeclaration {

  public HaskellInstanceDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitInstanceDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellExpression getExpression() {
    return findChildByClass(HaskellExpression.class);
  }

  @Override
  @NotNull
  public List<HaskellIdecl> getIdeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellIdecl.class);
  }

  @Override
  @NotNull
  public HaskellInst getInst() {
    return findNotNullChildByClass(HaskellInst.class);
  }

  @Override
  @NotNull
  public HaskellQcon getQcon() {
    return findNotNullChildByClass(HaskellQcon.class);
  }

  @Override
  @Nullable
  public HaskellScontext getScontext() {
    return findChildByClass(HaskellScontext.class);
  }

  @Override
  @NotNull
  public List<HaskellVarId> getVarIdList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellVarId.class);
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

}
