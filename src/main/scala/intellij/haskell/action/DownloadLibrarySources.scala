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

package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util.HaskellEditorUtil

class DownloadLibrarySources extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, !StackProjectManager.isInitializing(_))
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    Option(actionEvent.getProject).foreach(project => {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell library sources and adding them as source libraries to module") {

        def run(progressIndicator: ProgressIndicator) {
          HaskellModuleBuilder.addLibrarySources(project)
        }
      })
    })
  }
}
