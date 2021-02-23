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

package intellij.haskell.settings

import com.intellij.openapi.module.{ModuleConfigurationEditor, ModuleType}
import com.intellij.openapi.roots.ui.configuration.{ClasspathEditor, ContentEntriesEditor, DefaultModuleEditorsProvider, ModuleConfigurationState}
import intellij.haskell.module.HaskellModuleType

class HaskellModuleEditorsProvider extends DefaultModuleEditorsProvider {

  override def createEditors(state: ModuleConfigurationState): Array[ModuleConfigurationEditor] = {
    val module = state.getRootModel.getModule
    if (!ModuleType.get(module).isInstanceOf[HaskellModuleType]) {
      ModuleConfigurationEditor.EMPTY
    } else {
      Array[ModuleConfigurationEditor](new ContentEntriesEditor(module.getName, state), new ClasspathEditor(state))
    }
  }
}