// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellQConQualifier2 extends HaskellQualifierElement {

    @NotNull
    List<HaskellConid> getConidList();

    String getName();

    PsiElement setName(String newName);

    HaskellNamedElement getNameIdentifier();

    PsiReference getReference();

    ItemPresentation getPresentation();

}
