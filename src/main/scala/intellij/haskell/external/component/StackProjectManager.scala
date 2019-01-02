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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootModificationUtil}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.WaitFor
import intellij.haskell.action.{HaskellReformatAction, HindentReformatAction, StylishHaskellReformatAction}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.execution.StackCommandLine.build
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.HaskellModuleBuilder
import intellij.haskell.util._
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

object StackProjectManager {

  import intellij.haskell.util.ScalaUtil._

  def isInitializing(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.initializing)
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

  def isPreloadingAllLibraryIdentifiers(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.preloadingAllLibraryIdentifiers)
  }

  def start(project: Project): Unit = {
    init(project)
  }

  def restart(project: Project): Unit = {
    HaskellFileUtil.saveAllFiles(project)

    init(project, restart = true)
  }

  def getStackProjectManager(project: Project): Option[StackProjectManager] = {
    project.isDisposed.optionNot(project.getComponent(classOf[StackProjectManager]))
  }

  def getProjectLibraryFileWatcher(project: Project): Option[ProjectLibraryFileWatcher] = {
    getStackProjectManager(project).map(_.projectLibraryFileWatcher)
  }

  def installHaskellTools(project: Project, update: Boolean): Unit = {
    getStackProjectManager(project).foreach(_.installingHaskellTools = true)

    val title = (if (update) "Updating" else "Installing") + " Haskell tools"

    ProgressManager.getInstance().run(new Task.Backgroundable(project, title, false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

      private def isToolAvailable(progressIndicator: ProgressIndicator, toolName: String) = {
        val toolNameExe = if (SystemInfo.isWindows) toolName + ".exe" else toolName
        if (!GlobalInfo.toolPath(toolNameExe).exists() || update) {
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

          getStackProjectManager(project).foreach(_.stylishHaskellAvailable = isToolAvailable(progressIndicator, StylishHaskellReformatAction.StylishHaskellName))

          getStackProjectManager(project).foreach(_.hindentAvailable = isToolAvailable(progressIndicator, HindentReformatAction.HindentName))
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
        getStackProjectManager(project).foreach(_.initializing = true)
        if (restart) {
          HaskellNotificationGroup.logInfoEvent(project, "Restarting Haskell project")
        } else {
          HaskellNotificationGroup.logInfoEvent(project, "Initializing Haskell project")
        }

        installHaskellTools(project, update = false)

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project, starting REPL(s) and preloading cache", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

          def run(progressIndicator: ProgressIndicator) {
            try {
              progressIndicator.setText("Busy with building project's dependencies")
              val dependenciesBuildResult = StackCommandLine.buildProjectDependenciesInMessageView(project)

              if (dependenciesBuildResult.contains(true)) {
                progressIndicator.setText("Busy with building project")
                val projectLibTargets = HaskellComponentsManager.findStackComponentInfos(project).filter(_.stanzaType == LibType).map(_.target)
                StackCommandLine.buildProjectInMessageView(project, projectLibTargets)
              } else {
                HaskellNotificationGroup.logErrorBalloonEvent(project, "Project will not be built because building it's dependencies failed")
              }

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
              }

              if (!project.isDisposed) {
                progressIndicator.setText(s"Busy with building Intero")
                build(project, Seq("intero"))
              }

              progressIndicator.setText("Busy with starting project REPLs")
              val replLoads = StackReplsManager.getReplsManager(project).map(_.stackComponentInfos.filter(_.stanzaType == LibType).flatMap { info =>
                StackReplsManager.getProjectRepl(project, info).map(repl => {
                  val replLoad = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                    repl.load(info.exposedModuleNames)
                  })
                  Thread.sleep(5000) // Have to wait between starting the REPLs otherwise timeouts while starting
                  replLoad
                })
              })

              progressIndicator.setText("Busy with starting global Stack REPL")
              StackReplsManager.getGlobalRepl(project)

              progressIndicator.setText("Busy with preloading global project info")
              GlobalProjectInfoComponent.findGlobalProjectInfo(project)

              progressIndicator.setText("Busy with preloading library packages info")
              LibraryPackageInfoComponent.preloadLibraryPackageInfos(project)

              progressIndicator.setText("Busy with preloading stack component info cache")
              val preloadStackComponentInfoCache = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable {
                HaskellComponentsManager.preloadStackComponentInfoCache(project)
              })

              val preloadLibraryFilesCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                progressIndicator.setText("Busy with downloading library sources")
                HaskellModuleBuilder.addLibrarySources(project, update = restart)

                HaskellComponentsManager.preloadLibraryFilesCache(project)
              })

              progressIndicator.setText("Busy with preloading library identifiers")
              val preloadLibraryIdentifiersCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                HaskellComponentsManager.preloadLibraryIdentifiersCaches(project)
              })

              progressIndicator.setText("Busy with preloading all library identifiers")
              ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                getStackProjectManager(project).foreach(_.preloadingAllLibraryIdentifiers = true)
                try {
                  HaskellComponentsManager.preloadAllLibraryIdentifiersCaches(project)

                  if (!project.isDisposed) {
                    HaskellNotificationGroup.logInfoEvent(project, "Restarting global REPL to release memory")
                    StackReplsManager.getGlobalRepl(project).foreach(_.restart())
                  }
                } finally {
                  getStackProjectManager(project).foreach(_.preloadingAllLibraryIdentifiers = false)
                }
              })

              if (!project.isDisposed) {
                getStackProjectManager(project).map(_.projectLibraryFileWatcher).foreach { watcher =>
                  ApplicationManager.getApplication.getMessageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, watcher)
                }
              }

              progressIndicator.setText("Busy with preloading caches")
              if (!preloadLibraryFilesCacheFuture.isDone || !preloadStackComponentInfoCache.isDone || !preloadLibraryIdentifiersCacheFuture.isDone || replLoads.exists(_.exists(!_.isDone))) {
                FutureUtil.waitForValue(project, preloadStackComponentInfoCache, "preloading library caches", 600)
                FutureUtil.waitForValue(project, preloadLibraryFilesCacheFuture, "preloading library caches", 600)
                FutureUtil.waitForValue(project, preloadLibraryIdentifiersCacheFuture, "preloading library caches", 600)

                progressIndicator.setText("Busy with loading project modules in REPLs")
                replLoads.foreach { rl =>
                  new WaitFor(300000, 1000) {
                    override def condition(): Boolean = {
                      rl.forall(_.isDone)
                    }
                  }

                  if (!rl.forall(_.isDone)) {
                    HaskellNotificationGroup.logInfoEvent(project, "Not all project modules are loaded")
                  }
                }
              }
            } finally {
              getStackProjectManager(project).foreach(_.initializing = false)
            }

            // Force to load the module in REPL when REPL can be started. It could have happen that IntelliJ wanted to load file (via HaskellAnnotator)
            // but REPL could not yet be started.
            HaskellAnnotator.NotLoadedFile.remove(project) foreach { psiFile =>
              HaskellNotificationGroup.logInfoEvent(project, s"${psiFile.getName} will be forced loaded")
              HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
            }

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
  private var preloadingAllLibraryIdentifiers = false

  @volatile
  private var replsManager: Option[StackReplsManager] = None

  private val projectLibraryFileWatcher = new ProjectLibraryFileWatcher(project)

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
      HaskellComponentsManager.invalidateGlobalCaches(project)
    }
  }

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      disableDefaultReformatAction()
      initStackReplsManager()
      if (replsManager.exists(_.stackComponentInfos.isEmpty)) {
        Messages.showErrorDialog(project, s"Can not start project because no Cabal file was found or could not be read", "Can not start project")
      } else {
        StackProjectManager.start(project)
      }
    }
  }

  override def disposeComponent(): Unit = {}

  private def disableDefaultReformatAction(): Unit = {
    val actionManager = ActionManager.getInstance
    // Overriding IntelliJ's default shortcut for formatting
    actionManager.unregisterAction("ReformatCode")
    actionManager.registerAction("ReformatCode", new HaskellReformatAction)
  }
}