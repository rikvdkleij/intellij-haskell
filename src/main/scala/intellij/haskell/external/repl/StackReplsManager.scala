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

package intellij.haskell.external.repl

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.StackReplsComponentsManager
import intellij.haskell.external.repl.process.{GlobalStackReplProcess, ProjectStackReplProcess}
import intellij.haskell.util.HaskellProjectUtil

object StackReplsManager {

  def getProjectRepl(project: Project) = {
    projectRepl(project)
  }

  def getGlobalRepl(project: Project) = {
    globalRepl(project)
  }

  private def projectRepl(project: Project) = {
    project.getComponent(classOf[ProjectStackReplProcess])
  }

  private def globalRepl(project: Project) = {
    project.getComponent(classOf[GlobalStackReplProcess])
  }
}

class StackReplsManager(project: Project) extends ProjectComponent {
  override def getComponentName: String = "stack-repls-manager"

  override def projectClosed(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    ProgressManager.getInstance().run(new Task.Backgroundable(project, s"[$getComponentName] Starting Stack repls, building project and preloading cache", false) {

      def run(progressIndicator: ProgressIndicator) {
        if (HaskellProjectUtil.isHaskellStackProject(project)) {
          progressIndicator.setText("Busy with starting Stack repls and building project")
          StackReplsManager.getProjectRepl(project).start()
          StackReplsManager.getGlobalRepl(project).start()

          progressIndicator.setText("Busy with preloading cache")
          StackReplsComponentsManager.preloadModuleIdentifiersCaches(project)
        }
      }
    })
  }

  override def disposeComponent(): Unit = {}
}
