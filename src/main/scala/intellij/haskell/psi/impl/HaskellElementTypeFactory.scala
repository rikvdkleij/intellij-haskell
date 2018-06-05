/*
 * Copyright 2014-2018 Rik van der Kleij
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

package intellij.haskell.psi.impl

import com.intellij.psi.tree.IElementType
import intellij.haskell.psi.stubs.types._

object HaskellElementTypeFactory {

  def factory(name: String): IElementType = {
    name match {
      case "HS_CONID" => new HaskellConidStubElementType(name)
      case "HS_VARID" => new HaskellVaridStubElementType(name)
      case "HS_VARSYM" => new HaskellVarsymStubElementType(name)
      case "HS_CONSYM" => new HaskellConsymStubElementType(name)
      case "HS_MODID" => new HaskellModidStubElementType(name)
      case _ => throw new IllegalStateException(s"Unknown element name: $name")
    }
  }
}
