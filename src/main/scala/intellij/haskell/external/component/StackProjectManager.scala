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

import javax.swing.event.HyperlinkEvent

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.external.component.HaskellToolsComponent.HaskellToolName
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util.HaskellProjectUtil

object StackProjectManager {
  val componentName = "stack-repls-manager"

  val lts80Link = "\"https://www.stackage.org/lts-8.0\""
  val nightly20170114Link = "\"https://www.stackage.org/nightly-2017-02-13\""
  val haskelltoolsLink = "\"http://haskelltools.org/\""

  import intellij.haskell.util.ScalaUtil._

  def isStarting(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.starting)
  }

  def isHoogleAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.hoogleAvailable)
  }

  def isHlintAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.hlintAvailable)
  }

  def isHaskellToolsAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.haskellToolsAvailable)
  }

  def start(project: Project): Unit = {
    initialize(project)
  }

  def restart(project: Project): Unit = {
    initialize(project, restart = true)
  }

  private def getStackProjectManager(project: Project) = {
    project.isDisposed.optionNot(project.getComponent(classOf[StackProjectManager]))
  }

  private def initialize(project: Project, restart: Boolean = false): Unit = {
    if (HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = true)) {
      if (isStarting(project)) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack REPLs are already (re)starting")
      } else {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, s"[$componentName] Starting Stack REPLs, building tools and preloading cache", false) {

          def run(progressIndicator: ProgressIndicator) {
            getStackProjectManager(project).foreach(_.starting = true)
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

                  progressIndicator.setText("Busy with starting Stack REPLs")
                  projectRepl.start()
                  globalRepl.start()

                  progressIndicator.setText("Busy with preloading cache and building tools")
                  val preloadCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
                    override def run(): Unit = {
                      HaskellComponentsManager.preloadModuleIdentifiersCaches(project)

                      HaskellNotificationGroup.logInfoEvent(project, "Restarting global REPL to release memory")
                      globalRepl.restart()
                    }
                  })

                  StackCommandLine.executeBuild(project, Seq("build", HLintComponent.HlintName), "`hlint`")
                  getStackProjectManager(project).foreach(_.hlintAvailable = true)

                  StackCommandLine.executeBuild(project, Seq("build", HoogleComponent.HoogleName), "`hoogle`")
                  getStackProjectManager(project).foreach(_.hoogleAvailable = true)

                  if (HaskellToolsComponent.isRefactoringSupported(project)) {
                    StackCommandLine.runCommand(Seq("exec", "--", HaskellToolsComponent.HaskellToolName), project, logErrorAsInfo = true) match {
                      case Some(output) if output.getStderr.isEmpty => HaskellNotificationGroup.logInfoEvent(project, s"${HaskellToolsComponent.HaskellToolName} is already installed")
                      case _ => StackCommandLine.executeBuild(project, Seq("build") ++ HaskellToolsComponent.HaskellToolsCLIName, "`haskell-tools`")
                    }
                    getStackProjectManager(project).foreach(_.haskellToolsAvailable = true)
                  } else {
                    HaskellNotificationGroup.logInfoBalloonEvent(
                      project,
                      s"You need a Stack resolver greater than <a href=$lts80Link>lts-8.0</a> or <a href=$nightly20170114Link>nightly-2017-02-13</a> in order to work with <a href=$haskelltoolsLink>$HaskellToolName</a>.",
                      (_: Notification, hyperlinkEvent: HyperlinkEvent) => {
                        if (hyperlinkEvent.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
                          BrowserUtil.browse(hyperlinkEvent.getURL)
                        }
                      })
                  }

                  if (HoogleComponent.doesHoogleDatabaseExist(project)) {
                    HaskellNotificationGroup.logInfoEvent(project, "Rebuilding Hoogle database")
                    HoogleComponent.rebuildHoogle(project)
                  } else {
                    HoogleComponent.showHoogleDatabaseDoesNotExistNotification(project)
                  }

                  if (!preloadCacheFuture.isDone) {
                    preloadCacheFuture.get
                  }
                case _ => ()
              }
            }
            finally {
              getStackProjectManager(project).foreach(_.starting = false)
            }
          }
        })
      }
    }
  }
}

class StackProjectManager(project: Project) extends ProjectComponent {

  private var starting = false
  private var hoogleAvailable = false
  private var hlintAvailable = false
  private var haskellToolsAvailable = false


  override def getComponentName: String = StackProjectManager.componentName

  override def projectClosed(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = StackProjectManager.start(project)

  override def disposeComponent(): Unit = {}
}