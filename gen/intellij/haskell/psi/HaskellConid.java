// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import intellij.haskell.psi.stubs.HaskellConidStub;

public interface HaskellConid extends HaskellNamedElement, StubBasedPsiElement<HaskellConidStub> {

    String getName();

    PsiElement setName(String newName);

    HaskellNamedElement getNameIdentifier();

    PsiReference getReference();

    ItemPresentation getPresentation();

}
