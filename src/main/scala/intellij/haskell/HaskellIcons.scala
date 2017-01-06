/*
 * Copyright 2016 Rik van der Kleij
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

package intellij.haskell

import com.intellij.openapi.util.IconLoader

object HaskellIcons {
  final val HaskellSmallLogo = IconLoader.getIcon("/icons/haskell-small-logo.png")
  final val HaskellSmallBlueLogo = IconLoader.getIcon("/icons/haskell-small-blue-logo.png")
  final val HaskellLogo = IconLoader.getIcon("/icons/haskell_24x24.png")

  final val Module = IconLoader.getIcon("/icons/module.png")
  final val Data = IconLoader.getIcon("/icons/data.png")
  final val NewType = IconLoader.getIcon("/icons/newtype.png")
  final val Type = IconLoader.getIcon("/icons/type.png")
  final val Class = IconLoader.getIcon("/icons/class.png")
  final val Default = IconLoader.getIcon("/icons/default_declaration.png")
  final val TypeFamily = IconLoader.getIcon("/icons/type_family.png")
  final val TypeInstance = IconLoader.getIcon("/icons/type_instance.png")
  final val TypeSignature = IconLoader.getIcon("/icons/type_signature.png")
  final val Instance = IconLoader.getIcon("/icons/instance.png")
  final val Foreign = IconLoader.getIcon("/icons/foreign.png")

  final val CabalLogo = IconLoader.getIcon("/icons/cabal.png")

  final val REPL = IconLoader.getIcon("/icons/repl.png")
}