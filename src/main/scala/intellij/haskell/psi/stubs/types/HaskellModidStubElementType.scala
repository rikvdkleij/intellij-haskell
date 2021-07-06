/*
 * Copyright 2014-2020 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.psi.stubs.types

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.{StubElement, StubInputStream}
import intellij.haskell.psi.HaskellModid
import intellij.haskell.psi.impl.HaskellModidImpl
import intellij.haskell.psi.stubs.HaskellModidStub

class HaskellModidStubElementType(debugName: String) extends HaskellNamedStubElementType[HaskellModidStub, HaskellModid](debugName) {
  def createPsi(stub: HaskellModidStub): HaskellModid = {
    new HaskellModidImpl(stub, this)
  }

  def createStub(psi: HaskellModid, parentStub: StubElement[_]): HaskellModidStub = {
    new HaskellModidStub(parentStub, this, psi.getName)
  }

  def deserialize(dataStream: StubInputStream, parentStub: StubElement[_ <: PsiElement]): HaskellModidStub = {
    new HaskellModidStub(parentStub, this, dataStream.readName)
  }
}