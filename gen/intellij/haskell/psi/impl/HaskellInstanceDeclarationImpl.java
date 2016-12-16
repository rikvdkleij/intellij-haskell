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
  public HaskellCidecls getCidecls() {
    return PsiTreeUtil.getChildOfType(this, HaskellCidecls.class);
  }

  @Override
  @Nullable
  public HaskellIncoherentPragma getIncoherentPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellIncoherentPragma.class);
  }

  @Override
  @NotNull
  public HaskellInst getInst() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellInst.class));
  }

  @Override
  @Nullable
  public HaskellOverlapPragma getOverlapPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellOverlapPragma.class);
  }

  @Override
  @NotNull
  public HaskellQName getQName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, HaskellQName.class));
  }

  @Override
  @Nullable
  public HaskellScontext getScontext() {
    return PsiTreeUtil.getChildOfType(this, HaskellScontext.class);
  }

  @Override
  @NotNull
  public List<HaskellVarCon> getVarConList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellVarCon.class);
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
