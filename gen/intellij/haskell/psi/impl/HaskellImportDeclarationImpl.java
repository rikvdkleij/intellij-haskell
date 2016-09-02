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
    return findChildByClass(HaskellImportPackageName.class);
  }

  @Override
  @Nullable
  public HaskellImportQualified getImportQualified() {
    return findChildByClass(HaskellImportQualified.class);
  }

  @Override
  @Nullable
  public HaskellImportQualifiedAs getImportQualifiedAs() {
    return findChildByClass(HaskellImportQualifiedAs.class);
  }

  @Override
  @Nullable
  public HaskellImportSpec getImportSpec() {
    return findChildByClass(HaskellImportSpec.class);
  }

  @Override
  @Nullable
  public HaskellModid getModid() {
    return findChildByClass(HaskellModid.class);
  }

  @Override
  @Nullable
  public HaskellSourcePragma getSourcePragma() {
    return findChildByClass(HaskellSourcePragma.class);
  }

  public Option<String> getModuleName() {
    return HaskellPsiImplUtil.getModuleName(this);
  }

}
