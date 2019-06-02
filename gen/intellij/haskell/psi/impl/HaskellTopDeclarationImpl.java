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

public class HaskellTopDeclarationImpl extends HaskellCompositeElementImpl implements HaskellTopDeclaration {

  public HaskellTopDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitTopDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellClassDeclaration getClassDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellClassDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellDataDeclaration getDataDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellDataDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellDefaultDeclaration getDefaultDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellDefaultDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellDerivingDeclaration getDerivingDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellDerivingDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, HaskellExpression.class);
  }

  @Override
  @Nullable
  public HaskellFixityDeclaration getFixityDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellFixityDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellForeignDeclaration getForeignDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellForeignDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellInstanceDeclaration getInstanceDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellInstanceDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellNewtypeDeclaration getNewtypeDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellNewtypeDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellPragma getPragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellPragma.class);
  }

  @Override
  @Nullable
  public HaskellTypeDeclaration getTypeDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellTypeFamilyDeclaration getTypeFamilyDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeFamilyDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellTypeInstanceDeclaration getTypeInstanceDeclaration() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeInstanceDeclaration.class);
  }

  @Override
  @Nullable
  public HaskellTypeSignature getTypeSignature() {
    return PsiTreeUtil.getChildOfType(this, HaskellTypeSignature.class);
  }

}
