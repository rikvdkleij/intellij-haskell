// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import intellij.haskell.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

public class HaskellImportDeclarationImpl extends HaskellCompositeElementImpl implements HaskellImportDeclaration {

  public HaskellImportDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HaskellVisitor visitor) {
    visitor.visitImportDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HaskellVisitor) accept((HaskellVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HaskellImportPackageName getImportPackageName() {
    return PsiTreeUtil.getChildOfType(this, HaskellImportPackageName.class);
  }

  @Override
  @Nullable
  public HaskellImportQualified getImportQualified() {
    return PsiTreeUtil.getChildOfType(this, HaskellImportQualified.class);
  }

  @Override
  @Nullable
  public HaskellImportQualifiedAs getImportQualifiedAs() {
    return PsiTreeUtil.getChildOfType(this, HaskellImportQualifiedAs.class);
  }

  @Override
  @Nullable
  public HaskellImportSpec getImportSpec() {
    return PsiTreeUtil.getChildOfType(this, HaskellImportSpec.class);
  }

  @Override
  @Nullable
  public HaskellModid getModid() {
    return PsiTreeUtil.getChildOfType(this, HaskellModid.class);
  }

  @Override
  @Nullable
  public HaskellSourcePragma getSourcePragma() {
    return PsiTreeUtil.getChildOfType(this, HaskellSourcePragma.class);
  }

  public Option<String> getModuleName() {
    return HaskellPsiImplUtil.getModuleName(this);
  }

}
