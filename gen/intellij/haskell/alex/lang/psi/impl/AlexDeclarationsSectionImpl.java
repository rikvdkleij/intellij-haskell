// This is a generated file. Not intended for manual editing.
package intellij.haskell.alex.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static intellij.haskell.alex.lang.psi.AlexTypes.*;
import intellij.haskell.alex.lang.psi.*;

public class AlexDeclarationsSectionImpl extends AlexElementImpl implements AlexDeclarationsSection {

  public AlexDeclarationsSectionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AlexVisitor visitor) {
    visitor.visitDeclarationsSection(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AlexVisitor) accept((AlexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AlexDeclaration> getDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AlexDeclaration.class);
  }

}
