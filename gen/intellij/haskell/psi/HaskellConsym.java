// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.SearchScope;
import intellij.haskell.psi.stubs.HaskellConsymStub;

public interface HaskellConsym extends HaskellNamedElement, StubBasedPsiElement<HaskellConsymStub> {

  String getName();

  PsiElement setName(String newName);

  HaskellNamedElement getNameIdentifier();

  PsiReference getReference();

  ItemPresentation getPresentation();

  SearchScope getUseScope();

}
