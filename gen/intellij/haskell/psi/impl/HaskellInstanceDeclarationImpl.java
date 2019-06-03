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
import scala.Option;
import scala.collection.Seq;

public class HaskellInstanceDeclarationImpl extends HaskellTopDeclarationImpl implements HaskellInstanceDeclaration {

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
  public HaskellCidecls getCidecls() {
    return PsiTreeUtil.getChildOfType(this, HaskellCidecls.class);
  }

  @Override
  @Nullable
  public HaskellInst getInst() {
    return PsiTreeUtil.getChildOfType(this, HaskellInst.class);
  }

  @Override
  @Nullable
  public HaskellQName getQName() {
    return PsiTreeUtil.getChildOfType(this, HaskellQName.class);
  }

  @Override
  @Nullable
  public HaskellScontext getScontext() {
    return PsiTreeUtil.getChildOfType(this, HaskellScontext.class);
  }

  @Override
  @Nullable
  public HaskellTypeEquality getTypeEquality() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeEquality.class);
  }

  @Override
  @NotNull
  public List<HaskellVarCon> getVarConList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellVarCon.class);
  }

  @Override
  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  @Override
  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  @Override
  public Seq<HaskellNamedElement> getIdentifierElements() {
    return HaskellPsiImplUtil.getIdentifierElements(this);
  }

  @Override
  public Option<String> getModuleName() {
    return HaskellPsiImplUtil.getModuleName(this);
  }

}
