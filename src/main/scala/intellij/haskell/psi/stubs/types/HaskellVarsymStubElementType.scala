package intellij.haskell.psi.stubs.types

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.{StubElement, StubInputStream}
import intellij.haskell.psi.HaskellVarsym
import intellij.haskell.psi.impl.HaskellVarsymImpl
import intellij.haskell.psi.stubs.HaskellVarsymStub

class HaskellVarsymStubElementType(debugName: String) extends HaskellNamedStubElementType[HaskellVarsymStub, HaskellVarsym](debugName) {
  def createPsi(stub: HaskellVarsymStub): HaskellVarsym = {
    new HaskellVarsymImpl(stub, this)
  }

  def createStub(psi: HaskellVarsym, parentStub: StubElement[_ <: PsiElement]): HaskellVarsymStub = {
    new HaskellVarsymStub(parentStub, this, psi.getName)
  }

  def deserialize(dataStream: StubInputStream, parentStub: StubElement[_ <: PsiElement]): HaskellVarsymStub = {
    new HaskellVarsymStub(parentStub, this, dataStream.readName)
  }
}