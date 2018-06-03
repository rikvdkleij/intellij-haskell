package intellij.haskell.cabal.lang.psi.impl

import com.intellij.psi.PsiElement
import intellij.haskell.cabal.lang.psi.ModulePart

trait ModuleImpl extends PsiElement {

  def getParts: Array[ModulePart] = getChildren.map(assertModulePart)

  def getFirstPart: ModulePart = assertModulePart(getFirstChild)

  def getLastPart: ModulePart = assertModulePart(getLastChild)

  def getModuleName: String = {
    this.getText
  }

  private def assertModulePart(el: PsiElement): ModulePart = el match {
    case el: ModulePart => el
    case other => throw new CabalElementTypeError("ModulePart", other)
  }
}
