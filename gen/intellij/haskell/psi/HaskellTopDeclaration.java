// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HaskellTopDeclaration extends HaskellCompositeElement {

  @Nullable
  HaskellCfilesPragma getCfilesPragma();

  @Nullable
  HaskellClassDeclaration getClassDeclaration();

  @Nullable
  HaskellDataDeclaration getDataDeclaration();

  @Nullable
  HaskellDefaultDeclaration getDefaultDeclaration();

  @Nullable
  HaskellDerivingDeclaration getDerivingDeclaration();

  @Nullable
  HaskellExpression getExpression();

  @Nullable
  HaskellFixityDeclaration getFixityDeclaration();

  @Nullable
  HaskellForeignDeclaration getForeignDeclaration();

  @Nullable
  HaskellInstanceDeclaration getInstanceDeclaration();

  @Nullable
  HaskellNewtypeDeclaration getNewtypeDeclaration();

  @Nullable
  HaskellOtherPragma getOtherPragma();

  @Nullable
  HaskellQqExpression getQqExpression();

  @Nullable
  HaskellQuasiQuote getQuasiQuote();

  @Nullable
  HaskellTypeDeclaration getTypeDeclaration();

  @Nullable
  HaskellTypeFamilyDeclaration getTypeFamilyDeclaration();

  @Nullable
  HaskellTypeInstanceDeclaration getTypeInstanceDeclaration();

  @Nullable
  HaskellTypeSignature getTypeSignature();

}
