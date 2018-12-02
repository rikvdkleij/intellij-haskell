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

public class AlexStatefulTokensRuleImpl extends AlexElementImpl implements AlexStatefulTokensRule {

  public AlexStatefulTokensRuleImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AlexVisitor visitor) {
    visitor.visitStatefulTokensRule(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AlexVisitor) accept((AlexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AlexIdentifier getIdentifier() {
    return findChildByClass(AlexIdentifier.class);
  }

  @Override
  @NotNull
  public List<AlexStatelessTokensRule> getStatelessTokensRuleList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AlexStatelessTokensRule.class);
  }

}
