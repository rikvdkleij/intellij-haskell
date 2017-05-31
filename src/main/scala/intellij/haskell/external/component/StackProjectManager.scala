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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util.HaskellProjectUtil

object StackProjectManager {
  val componentName = "stack-repls-manager"

  var starting = false

  def start(project: Project): Unit = {
    initialize(project)
  }

  def restart(project: Project): Unit = {
    initialize(project, restart = true)
  }

  private def initialize(project: Project, restart: Boolean = false): Unit = {
    if (HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = true)) {
      if (starting) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack REPLs are already (re)starting")
      } else {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, s"[$componentName] Starting Stack REPLs, building project, building tools and preloading cache", false) {

          def run(progressIndicator: ProgressIndicator) {
            starting = true
            try {
              (StackReplsManager.getProjectRepl(project), StackReplsManager.getGlobalRepl(project)) match {
                case (Some(projectRepl), Some(globalRepl)) =>
                  if (restart) {
                    progressIndicator.setText("Busy with stopping Stack REPLs")
                    globalRepl.exit()
                    projectRepl.exit()

                    progressIndicator.setText("Busy with cleaning up cache")
                    HaskellComponentsManager.invalidateGlobalCaches(project)

                    Thread.sleep(1000)
                  }

                  progressIndicator.setText("Busy with downloading library sources")
                  HaskellProjectUtil.getProjectModules(project).foreach(m => {
                    HaskellModuleBuilder.addLibrarySources(m)
                  })

                  progressIndicator.setText("Busy with building project and starting Stack REPLs")
                  projectRepl.start()
                  globalRepl.start()

                  progressIndicator.setText("Busy with preloading cache, building tools and/or rebuilding Hoogle database")
                  val preloadCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
                    override def run(): Unit = {
                      HaskellComponentsManager.preloadModuleIdentifiersCaches(project)

                      HaskellNotificationGroup.logInfoEvent(project, "Restarting global REPL to release memory")
                      globalRepl.restart()
                    }
                  })

                  StackCommandLine.executeBuild(project, Seq("build", HoogleComponent.HoogleName, HLintComponent.HlintName), "Build of `hoogle`, `hlint`")

                  //              val buildToolsFuture = ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
                  //                override def run(): Unit = {
                  //                  if (HaskellToolComponent.checkResolverForHaskellTools(project)) {
                  //                    StackCommandLine.executeBuild(project, Seq("build", HaskellToolComponent.HaskellToolsCLIName), "Build of `haskell-tools`")
                  //                  }
                  //                }
                  //              })

                  HoogleComponent.rebuildHoogle(project)

                  if (
                  //                !buildToolsFuture.isDone ||
                    !preloadCacheFuture.isDone) {
                    //                buildToolsFuture.get
                    preloadCacheFuture.get
                  }
                case _ => ()
              }
            }

            finally {
              starting = false
            }
          }
        }
        )
      }
    }
  }

}

class StackProjectManager(project: Project) extends ProjectComponent {

  override def getComponentName: String = StackProjectManager.componentName

  override def projectClosed(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = StackProjectManager.start(project)

  override def disposeComponent(): Unit = {}
}