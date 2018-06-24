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

package intellij.haskell.external.component

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootModificationUtil}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import intellij.haskell.action.{HindentFormatAction, StylishHaskellFormatAction}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.execution.StackCommandLine.build
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util.{FutureUtil, HaskellFileUtil, HaskellProjectUtil, ScalaUtil}
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

object StackProjectManager {

  import intellij.haskell.util.ScalaUtil._

  def isInitializing(project: Project): Boolean = {
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

  def isStylishHaskellAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.stylishHaskellAvailable)
  }

  def isHindentAvailable(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.hindentAvailable)
  }

  def isInstallingHaskellTools(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.installingHaskellTools)
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

  def installHaskellTools(project: Project, update: Boolean): Unit = {
    getStackProjectManager(project).foreach(_.installingHaskellTools = true)

    val title = (if (update) "Updating" else "Installing") + " Haskell tools"

    ProgressManager.getInstance().run(new Task.Backgroundable(project, title, false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

      private def isToolAvailable(progressIndicator: ProgressIndicator, toolName: String) = {
        if (!GlobalInfo.toolPath(toolName).toFile.exists() || update) {
          progressIndicator.setText(s"Busy with installing $toolName in ${GlobalInfo.toolsBinPath}")
          StackCommandLine.installTool(project, toolName)
        } else {
          true
        }
      }

      override def run(progressIndicator: ProgressIndicator): Unit = {
        try {
          if (update) {
            progressIndicator.setText(s"Busy with updating Stack's package index")
            StackCommandLine.updateStackIndex(project)
          }

          getStackProjectManager(project).foreach(_.hlintAvailable = isToolAvailable(progressIndicator, HLintComponent.HLintName))

          getStackProjectManager(project).foreach(_.hoogleAvailable = isToolAvailable(progressIndicator, HoogleComponent.HoogleName))

          getStackProjectManager(project).foreach(_.stylishHaskellAvailable = isToolAvailable(progressIndicator, StylishHaskellFormatAction.StylishHaskellName))

          getStackProjectManager(project).foreach(_.hindentAvailable = isToolAvailable(progressIndicator, HindentFormatAction.HindentName))
        } finally {
          getStackProjectManager(project).foreach(_.installingHaskellTools = false)
        }
      }
    })
  }

  private def init(project: Project, restart: Boolean = false): Unit = {
    if (HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = true)) {
      if (isInitializing(project)) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, "Action is not possible because project is initializing")
      } else {
        HaskellNotificationGroup.logInfoEvent(project, "Initializing Haskell project")
        getStackProjectManager(project).foreach(_.initializing = true)
        getStackProjectManager(project).foreach(_.building = true)

        installHaskellTools(project, update = false)

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project, starting REPL(s) and preloading cache", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

          def run(progressIndicator: ProgressIndicator) {
            try {
              try {
                progressIndicator.setText("Busy with building project's dependencies")
                val dependenciesBuildResult = StackCommandLine.buildProjectDependenciesInMessageView(project)

                progressIndicator.setText(s"Busy with building Intero")
                build(project, Seq("intero"), logBuildResult = true)

                if (dependenciesBuildResult.contains(true)) {
                  progressIndicator.setText("Busy with building project")
                  StackCommandLine.buildProjectInMessageView(project)
                } else {
                  HaskellNotificationGroup.logErrorBalloonEvent(project, "Project will not be built because building it's dependencies failed")
                }

                ApplicationManager.getApplication.getMessageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, new ProjectLibraryFileWatcher(project))

                if (restart) {
                  val projectRepsl = StackReplsManager.getRunningProjectRepls(project)
                  progressIndicator.setText("Busy with stopping Stack REPLs")
                  StackReplsManager.getGlobalRepl(project).foreach(_.exit())
                  projectRepsl.foreach(_.exit())

                  progressIndicator.setText("Busy with cleaning up cache")
                  HaskellComponentsManager.invalidateGlobalCaches(project)

                  ApplicationManager.getApplication.runReadAction(ScalaUtil.runnable {
                    getStackProjectManager(project).foreach(_.initStackReplsManager())
                  })

                  progressIndicator.setText("Busy with updating module settings")
                  StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos).foreach { moduleCabalInfos =>
                    moduleCabalInfos.foreach { case (module, cabalInfo) =>
                      ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
                        modifiableRootModel.getContentEntries.headOption.foreach { contentEntry =>
                          contentEntry.clearSourceFolders()
                          HaskellModuleBuilder.addSourceFolders(cabalInfo, contentEntry)
                        }
                      })
                    }
                  }

                  progressIndicator.setText("Busy with downloading library sources")
                  HaskellModuleBuilder.addLibrarySources(project, update = true)
                }

                progressIndicator.setText("Busy with starting global Stack REPL")
                StackReplsManager.getGlobalRepl(project).foreach(_.start())

                //  Force to load the module in REPL when REPL can be started. It could have happen that IntelliJ wanted to load file (via HaskellAnnotator)
                // but REPL could not be started.
                FileEditorManager.getInstance(project).getSelectedFiles foreach { vf =>
                  val psiFile = ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(HaskellFileUtil.convertToHaskellFile(project, vf)))
                  psiFile.foreach(pf => {
                    if (!LoadComponent.isFileLoaded(pf)) {
                      HaskellAnnotator.restartDaemonCodeAnalyzerForFile(pf)
                    }
                  })
                }
              } finally {
                getStackProjectManager(project).foreach(_.building = false)
              }

              progressIndicator.setText("Busy with downloading library sources")
              HaskellModuleBuilder.addLibrarySources(project, update = false)

              progressIndicator.setText("Busy with preloading libraries")
              val preloadCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                HaskellComponentsManager.preloadLibraryIdentifiersCaches(project)

                HaskellNotificationGroup.logInfoEvent(project, "Restarting global REPL to release memory")
                StackReplsManager.getGlobalRepl(project).foreach(_.restart())
              })

              if (!HoogleComponent.doesHoogleDatabaseExist(project)) {
                HoogleComponent.showHoogleDatabaseDoesNotExistNotification(project)
              }

              StackReplsManager.getReplsManager(project).foreach(_.moduleCabalInfos.foreach { case (module, cabalInfo) =>
                val intersection = cabalInfo.sourceRoots.toSeq.intersect(cabalInfo.testSourceRoots.toSeq)
                if (intersection.nonEmpty) {
                  intersection.foreach(p => {
                    val moduleName = module.getName
                    HaskellNotificationGroup.logWarningBalloonEvent(project, s"Source folder `$p` of module `$moduleName` is defined as Source and as Test Source")
                  })
                }
              })

              progressIndicator.setText("Busy with preloading libraries")
              if (!preloadCacheFuture.isDone) {
                FutureUtil.waitForValue(project, preloadCacheFuture, "preloading libraries", 600)
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
  private var stylishHaskellAvailable = false

  @volatile
  private var hindentAvailable = false

  @volatile
  private var installingHaskellTools = false

  @volatile
  private var replsManager: Option[StackReplsManager] = None

  def getStackReplsManager: Option[StackReplsManager] = {
    replsManager
  }

  def initStackReplsManager(): Unit = {
    replsManager = Option(new StackReplsManager(project))
  }

  override def projectClosed(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      replsManager.foreach(_.getGlobalRepl.exit())
      replsManager.foreach(_.getRunningProjectRepls.foreach(_.exit()))
    }
  }

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      initStackReplsManager()
      if (replsManager.exists(_.stackComponentInfos.isEmpty)) {
        Messages.showErrorDialog(project, s"Can not start project because no Cabal file was found or could not be read", "Can not start project")
      } else {
        StackProjectManager.start(project)
      }
    }
  }

  override def disposeComponent(): Unit = {}
}