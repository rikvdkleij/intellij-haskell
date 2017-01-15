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
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.HaskellProjectUtil

object RestartStackReplsAction {
  private var restarting = false

  def restart(project: Project): Unit = {
    if (restarting) {
      HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack repls are already restarting")
      return
    }

    Option(project) foreach { project =>
      restarting = true
      ProgressManager.getInstance().run(new Backgroundable(project, "Busy with restarting Stack repls", false) {
        def run(progressIndicator: ProgressIndicator) {
          try {
            (StackReplsManager.getProjectRepl(project), StackReplsManager.getGlobalRepl(project)) match {
              case (Some(projectRepl), Some(globalRepl)) =>
                progressIndicator.setText("Busy with stopping Stack repls")
                globalRepl.exit()
                projectRepl.exit()

                progressIndicator.setText("Busy with cleaning up")
                cleanLocalPackages(project)
                HaskellComponentsManager.invalidateGlobalCaches(project)

                Thread.sleep(1000)

                progressIndicator.setText("Busy with building project and starting Stack repls")
                projectRepl.start()
                globalRepl.start()

                progressIndicator.setText("Busy with preloading cache")
                HaskellComponentsManager.preloadModuleIdentifiersCaches(project)

                progressIndicator.setText("Restarting global repl to release memory")
                globalRepl.restart()
              case _ => ()
            }
          } finally {
            restarting = false
          }
        }
      })
    }
  }

  private def cleanLocalPackages(project: Project): Unit = {
    StackCommandLine.runCommand(Seq("clean"), project)
  }
}

class RestartStackReplsAction extends AnAction {

  override def update(e: AnActionEvent): Unit = {
    Option(e.getProject) match {
      case Some(p) =>
        val isHaskellProject = HaskellProjectUtil.isHaskellStackProject(p)
        e.getPresentation.setVisible(isHaskellProject)
        e.getPresentation.setEnabled(isHaskellProject && !RestartStackReplsAction.restarting)
      case None =>
        e.getPresentation.setEnabledAndVisible(false)
    }
  }

  override def actionPerformed(e: AnActionEvent): Unit = {
    RestartStackReplsAction.restart(e.getProject)
  }
}