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

package intellij.haskell.framework

import javax.swing.JComponent

import com.intellij.framework.FrameworkTypeEx
import com.intellij.framework.addSupport.{FrameworkSupportInModuleConfigurable, FrameworkSupportInModuleProvider}
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.{Module, ModuleType}
import com.intellij.openapi.roots.{ModifiableModelsProvider, ModifiableRootModel}
import intellij.haskell.module.HaskellModuleType

class HaskellFrameworkSupportProvider extends FrameworkSupportInModuleProvider {
  override def getFrameworkType: FrameworkTypeEx = HaskellFrameworkType.getInstance

  override def isEnabledForModuleType(moduleType: ModuleType[_ <: ModuleBuilder]): Boolean = moduleType.isInstanceOf[HaskellModuleType]

  override def createConfigurable(model: FrameworkSupportModel): FrameworkSupportInModuleConfigurable = {
    new FrameworkSupportInModuleConfigurable {
      override def createComponent(): JComponent = null

      override def addSupport(module: Module, rootModel: ModifiableRootModel, modifiableModelsProvider: ModifiableModelsProvider): Unit = {
      }
    }
  }
}
