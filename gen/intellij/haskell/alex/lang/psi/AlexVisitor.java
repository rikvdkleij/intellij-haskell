// This is a generated file. Not intended for manual editing.
package intellij.haskell.alex.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;

public class AlexVisitor extends PsiElementVisitor {

  public void visitDeclaration(@NotNull AlexDeclaration o) {
    visitElement(o);
  }

  public void visitDeclarationsSection(@NotNull AlexDeclarationsSection o) {
    visitElement(o);
  }

  public void visitIdentifier(@NotNull AlexIdentifier o) {
    visitElement(o);
  }

  public void visitRegex(@NotNull AlexRegex o) {
    visitElement(o);
  }

  public void visitRegexPart(@NotNull AlexRegexPart o) {
    visitElement(o);
  }

  public void visitRuleDeclaration(@NotNull AlexRuleDeclaration o) {
    visitElement(o);
  }

  public void visitRuleDescription(@NotNull AlexRuleDescription o) {
    visitElement(o);
  }

  public void visitStatefulTokensRule(@NotNull AlexStatefulTokensRule o) {
    visitElement(o);
  }

  public void visitStatelessTokensRule(@NotNull AlexStatelessTokensRule o) {
    visitElement(o);
  }

  public void visitTokenSetDeclaration(@NotNull AlexTokenSetDeclaration o) {
    visitElement(o);
  }

  public void visitTokenSetId(@NotNull AlexTokenSetId o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitTokensRule(@NotNull AlexTokensRule o) {
    visitElement(o);
  }

  public void visitTokensSection(@NotNull AlexTokensSection o) {
    visitElement(o);
  }

  public void visitTopModuleSection(@NotNull AlexTopModuleSection o) {
    visitElement(o);
  }

  public void visitUserCodeSection(@NotNull AlexUserCodeSection o) {
    visitElement(o);
  }

  public void visitWrapperType(@NotNull AlexWrapperType o) {
    visitElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitElement(@NotNull AlexElement o) {
    super.visitElement(o);
  }

}
