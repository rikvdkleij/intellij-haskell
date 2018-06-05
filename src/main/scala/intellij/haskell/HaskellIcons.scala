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

package intellij.haskell

import com.intellij.openapi.util.IconLoader

object HaskellIcons {
  final val HaskellSmallLogo = IconLoader.getIcon("/icons/haskell@16.png")
  final val HaskellSmallBlueLogo = IconLoader.getIcon("/icons/haskell-blue@16.png")
  final val HaskellLogo = IconLoader.getIcon("/icons/haskell@24.png")

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
}