// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellQConQualifier4 extends HaskellQualifierElement, HaskellNamedElement {

    @NotNull
    List<HaskellConid> getConidList();

    String getName();

    PsiElement setName(String newName);

    HaskellNamedElement getNameIdentifier();

    PsiReference getReference();

    ItemPresentation getPresentation();

}
