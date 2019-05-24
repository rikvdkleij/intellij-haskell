// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellTypeFamilyType extends HaskellCompositeElement {

  @NotNull
  List<HaskellCcontext> getCcontextList();

  @NotNull
  List<HaskellQName> getQNameList();

  @NotNull
  List<HaskellQNames> getQNamesList();

  @NotNull
  List<HaskellTtype> getTtypeList();

}
