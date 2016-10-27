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
import intellij.haskell.external.{HLintComponent, HaskellDocumentationProvider}
import intellij.haskell.util.{HaskellProjectUtil, StackUtil}

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
          progressIndicator.setText("Busy with building project and starting Stack repls")
          StackReplsManager.getProjectRepl(project).start()
          StackReplsManager.getGlobalRepl(project).start()

          progressIndicator.setText("Busy with preloading cache")
          StackReplsComponentsManager.preloadModuleIdentifiersCaches(project)

          progressIndicator.setText("Restarting global repl to release memory")
          StackReplsManager.getGlobalRepl(project).restart()

          // TODO stack build stylish-haskell hindent hlint intero haskell-docs apply-refact
          StackUtil.executeBuild(project, Seq("build", HaskellDocumentationProvider.HaskellDocsName), "build of Haskell-docs")
          StackUtil.executeBuild(project, Seq("build", HLintComponent.HlintName), "build of Hlint")
        }
      }
    })
  }

  override def disposeComponent(): Unit = {}
}
