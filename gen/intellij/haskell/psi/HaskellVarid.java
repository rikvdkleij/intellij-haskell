// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import intellij.haskell.psi.stubs.HaskellVaridStub;

public interface HaskellVarid extends HaskellNamedElement, StubBasedPsiElement<HaskellVaridStub> {

  String getName();

  PsiElement setName(String newName);

  HaskellNamedElement getNameIdentifier();

  PsiReference getReference();

  ItemPresentation getPresentation();

}
