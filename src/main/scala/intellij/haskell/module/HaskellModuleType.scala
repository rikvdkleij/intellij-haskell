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

package intellij.haskell.module

import com.intellij.openapi.module.{ModuleType, ModuleTypeManager}
import icons.HaskellIcons
import javax.swing.Icon

class HaskellModuleType extends ModuleType[HaskellModuleBuilder](HaskellModuleType.Id) {

  def createModuleBuilder(): HaskellModuleBuilder = new HaskellModuleBuilder

  def getName: String = "Haskell module"

  def getDescription: String = "Haskell module for Haskell project"

  def getNodeIcon(isOpened: Boolean): Icon = HaskellIcons.HaskellLogo

  def getBigIcon: Icon = HaskellIcons.HaskellLogo
}

object HaskellModuleType {
  val Id = "HASKELL_MODULE"

  def getInstance: HaskellModuleType = {
    ModuleTypeManager.getInstance.findByID(Id).asInstanceOf[HaskellModuleType]
  }

}
