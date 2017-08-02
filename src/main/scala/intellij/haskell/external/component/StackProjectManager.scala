/*
 * Copyright 2014-2017 Rik van der Kleij
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
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.execution.StackCommandLine.executeBuild
import intellij.haskell.external.repl.{GlobalStackRepl, StackReplsManager}
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util.HaskellProjectUtil

object StackProjectManager {

  import intellij.haskell.util.ScalaUtil._

  def isInitialzing(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.initializing)
  }

  def isBuilding(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.building)
  }

  def isHoogleAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.hoogleAvailable)
  }

  def isHlintAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.hlintAvailable)
  }

  def start(project: Project): Unit = {
    init(project)
  }

  def restart(project: Project): Unit = {
    init(project, restart = true)
  }

  def getStackProjectManager(project: Project): Option[StackProjectManager] = {
    project.isDisposed.optionNot(project.getComponent(classOf[StackProjectManager]))
  }

  private def init(project: Project, restart: Boolean = false): Unit = {
    if (HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = true)) {
      if (isInitialzing(project)) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack REPLs are already (re)starting")
      } else {
        HaskellNotificationGroup.logInfoEvent(project, "Initializing Haskell project")

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project, starting REPL(s), building tools and preloading cache", false, PerformInBackgroundOption.DEAF) {

          def run(progressIndicator: ProgressIndicator) {
            getStackProjectManager(project).foreach(_.initializing = true)
            getStackProjectManager(project).foreach(_.building = true)
            try {
              try {
                progressIndicator.setText("Busy with building project")

                StackCommandLine.buildProjectInMessageView(project, progressIndicator)

                if (restart) {
                  val projectRepl = StackReplsManager.getProjectRepl(project)
                  val projectTestRepl = StackReplsManager.getProjectTestRepl(project)
                  progressIndicator.setText("Busy with stopping Stack REPLs")
                  StackReplsManager.getGlobalRepl(project).foreach(_.exit())
                  projectRepl.foreach(_.exit())
                  projectRepl.foreach(_.clearLoadedInfo())
                  projectTestRepl.foreach(_.exit())
                  projectTestRepl.foreach(_.clearLoadedInfo())

                  progressIndicator.setText("Busy with cleaning up cache")
                  HaskellComponentsManager.invalidateGlobalCaches(project)

                  ApplicationManager.getApplication.runReadAction(new Runnable {

                    override def run(): Unit = {
                      getStackProjectManager(project).foreach(_.initStackReplsManager())
                    }
                  })
                }

                executeBuild(project, Seq("intero"), "intero", notifyBalloonError = true)

                progressIndicator.setText("Busy with starting global Stack REPL")
                StackReplsManager.getGlobalRepl(project).foreach(_.start())
              } finally {
                getStackProjectManager(project).foreach(_.building = false)
                ApplicationManager.getApplication.getMessageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, new ProjectLibraryFileWatcher(project))
              }

              progressIndicator.setText("Busy with downloading library sources")
              HaskellModuleBuilder.addLibrarySources(project)

              progressIndicator.setText("Busy with preloading libraries")
              val preloadCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(new Runnable {

                override def run(): Unit = {
                  HaskellComponentsManager.preloadLibraryIdentifiersCaches(project)

                  HaskellNotificationGroup.logInfoEvent(project, "Restarting global REPL to release memory")
                  StackReplsManager.getGlobalRepl(project).foreach(_.restart())
                }
              })

              progressIndicator.setText(s"Busy with building ${HLintComponent.HlintName}")
              StackCommandLine.executeBuild(project, Seq(HLintComponent.HlintName), HLintComponent.HlintName, notifyBalloonError = true)
              getStackProjectManager(project).foreach(_.hlintAvailable = true)

              progressIndicator.setText(s"Busy with building ${HoogleComponent.HoogleName}")
              StackCommandLine.executeBuild(project, Seq(HoogleComponent.HoogleName), HoogleComponent.HoogleName, notifyBalloonError = true)
              getStackProjectManager(project).foreach(_.hoogleAvailable = true)


              if (!HoogleComponent.doesHoogleDatabaseExist(project)) {
                HoogleComponent.showHoogleDatabaseDoesNotExistNotification(project)
              }

              progressIndicator.setText("Busy with preloading libraries")
              if (!preloadCacheFuture.isDone) {
                preloadCacheFuture.get
              }
            }
            finally {
              getStackProjectManager(project).foreach(_.initializing = false)
            }
          }
        })
      }
    }
  }
}

class StackProjectManager(project: Project) extends ProjectComponent {

  override def getComponentName: String = "stack-project-manager"

  @volatile
  private var initializing = false

  @volatile
  private var building = false

  @volatile
  private var hoogleAvailable = false

  @volatile
  private var hlintAvailable = false

  @volatile
  private var replsManager: StackReplsManager = _

  def getStackReplsManager: StackReplsManager = {
    replsManager
  }

  def initStackReplsManager(): Unit = {
    replsManager = new StackReplsManager(project, new GlobalStackRepl(project), None, None)
  }

  override def projectClosed(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      replsManager.getGlobalRepl.exit()
      replsManager.getProjectRepl.foreach(_.exit())
      replsManager.getProjectTestRepl.foreach(_.exit())
    }
  }

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      initStackReplsManager()
      if (replsManager.stackComponentInfos.isEmpty) {
        Messages.showErrorDialog(project, s"Can not start project because Stack/Cabal info can not be retrieved", "Can not start project")
      } else {
        StackProjectManager.start(project)
      }
    }
  }

  override def disposeComponent(): Unit = {}
}