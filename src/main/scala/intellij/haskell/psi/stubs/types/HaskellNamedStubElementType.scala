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

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.{IndexSink, NamedStubBase, StubOutputStream}
import intellij.haskell.psi._
import intellij.haskell.psi.stubs.index.HaskellAllNameIndex

abstract class HaskellNamedStubElementType[S <: NamedStubBase[T], T <: HaskellNamedElement](debugName: String) extends HaskellStubElementType[S, T](debugName) {

  def indexStub(stub: S, sink: IndexSink): Unit = {
    val name: String = stub.getName
    if (name != null) {
      sink.occurrence(HaskellAllNameIndex.Key, name)
    }
  }

  def serialize(stub: S, dataStream: StubOutputStream): Unit = {
    dataStream.writeName(stub.getName)
  }

  override def shouldCreateStub(node: ASTNode): Boolean = {
    node.getPsi match {
      case _: HaskellVarid => true
      case _: HaskellVarsym => true
      case _: HaskellConid => true
      case _: HaskellConsym => true
      case _: HaskellModid => true
      case _ => false
    }
  }
}