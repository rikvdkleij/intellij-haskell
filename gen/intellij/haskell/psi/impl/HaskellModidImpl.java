// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static intellij.haskell.psi.HaskellTypes.*;
import intellij.haskell.psi.stubs.HaskellModidStub;
import intellij.haskell.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;

public class HaskellModidImpl extends HaskellNamedStubBasedPsiElementBase<HaskellModidStub> implements HaskellModid {

  public HaskellModidImpl(HaskellModidStub stub, IStubElementType type) {
    super(stub, type);
  }

  public HaskellModidImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitModid(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  public String getName() {
    return HaskellPsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return HaskellPsiImplUtil.setName(this, newName);
  }

  public HaskellNamedElement getNameIdentifier() {
    return HaskellPsiImplUtil.getNameIdentifier(this);
  }

  public PsiReference getReference() {
    return HaskellPsiImplUtil.getReference(this);
  }

  public ItemPresentation getPresentation() {
    return HaskellPsiImplUtil.getPresentation(this);
  }

  public SearchScope getUseScope() {
    return HaskellPsiImplUtil.getUseScope(this);
  }

}
