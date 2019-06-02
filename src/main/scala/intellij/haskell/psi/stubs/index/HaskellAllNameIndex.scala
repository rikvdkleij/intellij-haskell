/*
 * Copyright 2014-2019 Rik van der Kleij
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

package intellij.haskell.psi.stubs.index

import com.intellij.psi.stubs.{StringStubIndexExtension, StubIndexKey}
import intellij.haskell.psi.HaskellNamedElement

object HaskellAllNameIndex {

  val Key: StubIndexKey[String, HaskellNamedElement] = StubIndexKey.createIndexKey("haskell.all.name")
  val Version = 1
}

class HaskellAllNameIndex extends StringStubIndexExtension[HaskellNamedElement] {

  override def getVersion: Int = {
    super.getVersion + HaskellAllNameIndex.Version
  }

  def getKey: StubIndexKey[String, HaskellNamedElement] = {
    HaskellAllNameIndex.Key
  }
}