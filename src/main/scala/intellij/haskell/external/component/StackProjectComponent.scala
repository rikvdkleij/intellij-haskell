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

package intellij.haskell.external.component

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.HaskellProjectUtil

class StackProjectComponent(project: Project) extends ProjectComponent {
  override def getComponentName: String = "stack-repls-manager"

  override def projectClosed(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellStackProject(project)) {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, s"[$getComponentName] Starting Stack repls, building project, building tools and preloading cache", false) {

        def run(progressIndicator: ProgressIndicator) {
          progressIndicator.setText("Busy with building project and starting Stack repls")
          StackReplsManager.getProjectRepl(project).start()
          StackReplsManager.getGlobalRepl(project).start()

          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
            override def run(): Unit = {
              StackCommandLine.executeBuild(project, Seq("build", HaskellDocumentationProvider.HaskellDocsName, HLintComponent.HlintName, "apply-refact"), "Build of `haskell-docs`, `hlint` and `apply-refact`")
            }
          })

          progressIndicator.setText("Busy with preloading cache")
          StackReplsComponentsManager.preloadModuleIdentifiersCaches(project)

          progressIndicator.setText("Restarting global repl to release memory")
          StackReplsManager.getGlobalRepl(project).restart()
        }
      })

    }
  }

  override def disposeComponent(): Unit = {}
}
