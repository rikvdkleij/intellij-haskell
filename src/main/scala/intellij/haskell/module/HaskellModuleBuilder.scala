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

package intellij.haskell.module

import javax.swing.Icon

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.module.{Module, ModuleType}
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModifiableRootModel
import intellij.haskell.HaskellIcons
import intellij.haskell.sdk.HaskellSdkType

class HaskellModuleBuilder extends ModuleBuilder with ModuleBuilderListener {

  override def moduleCreated(module: Module): Unit = {}

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = HaskellModuleType.getInstance

  override def isSuitableSdkType(sdkType: SdkTypeId): Boolean = {
    sdkType == HaskellSdkType.getInstance
  }

  override def getNodeIcon: Icon = HaskellIcons.HaskellSmallLogo

  override def setupRootModel(rootModel: ModifiableRootModel) {
    addListener(this)
  }
}
