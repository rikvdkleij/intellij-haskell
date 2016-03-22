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

public class HaskellNewconstrImpl extends HaskellCompositeElementImpl implements HaskellNewconstr {

  public HaskellNewconstrImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitNewconstr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellGtycon getGtycon() {
    return findChildByClass(HaskellGtycon.class);
  }

  @Override
  @Nullable
  public HaskellNewconstrFielddecl getNewconstrFielddecl() {
    return findChildByClass(HaskellNewconstrFielddecl.class);
  }

  @Override
  @Nullable
  public HaskellQcon getQcon() {
    return findChildByClass(HaskellQcon.class);
  }

  @Override
  @NotNull
  public List<HaskellQvar> getQvarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellQvar.class);
  }

  @Override
  @Nullable
  public HaskellQvarOp getQvarOp() {
    return findChildByClass(HaskellQvarOp.class);
  }

  @Override
  @NotNull
  public List<HaskellTtype> getTtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTtype.class);
  }

  @Override
  @NotNull
  public List<HaskellTypeSignatureDeclaration> getTypeSignatureDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HaskellTypeSignatureDeclaration.class);
  }

}
