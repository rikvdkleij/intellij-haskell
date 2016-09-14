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

package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external._
import intellij.haskell.external.component.StackReplsComponentsManager
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil

import scala.concurrent.SyncVar

class RestartStackRepls extends AnAction {

  val restarting: SyncVar[Boolean] = new SyncVar()

  override def update(e: AnActionEvent): Unit = {
    e.getPresentation.setVisible(HaskellProjectUtil.isHaskellStackProject(e.getProject))
    e.getPresentation.setEnabled(!restarting.isSet)
  }

  override def actionPerformed(e: AnActionEvent): Unit = {
    if (restarting.isSet) {
      HaskellNotificationGroup.notifyBalloonWarning("Stack repls are already restarting")
      return
    }

    Option(e.getProject) match {
      case None => ()
      case Some(project) =>
        restarting.put(true)
        ProgressManager.getInstance().run(new Backgroundable(project, "Busy with restarting Stack repls", false) {
          def run(progressIndicator: ProgressIndicator) {
            try {
              val projectRepl = StackReplsManager.getProjectRepl(project)
              val globalRepl = StackReplsManager.getGlobalRepl(project)

              progressIndicator.setText("Busy with stopping Stack repls")
              globalRepl.exit()
              projectRepl.exit()

              progressIndicator.setText("Busy with cleaning up")
              cleanLocalPackages(project)
              StackReplsComponentsManager.invalidateModuleIdentifierCaches(project)

              progressIndicator.setText("Busy with starting Stack repls and building project")
              globalRepl.start()
              projectRepl.start()

              progressIndicator.setText("Busy with preloading cache")
              StackReplsComponentsManager.preloadModuleIdentifiersCaches(project)
            } finally {
              restarting.take(100)
            }
          }
        })
    }
  }

  private def cleanLocalPackages(project: Project): Unit = {
    HaskellSdkType.getStackPath(project).foreach { stackPath =>
      CommandLine.getProcessOutput(
        project.getBasePath,
        stackPath,
        Seq("clean")
      )
    }
  }
}