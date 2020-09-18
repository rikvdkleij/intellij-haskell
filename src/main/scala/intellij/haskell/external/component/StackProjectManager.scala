/*
 * Copyright 2014-2020 Rik van der Kleij
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

import java.io.File

import com.intellij.ProjectTopics
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootEvent, ModuleRootListener, ModuleRootModificationUtil}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.{PsiTreeChangeAdapter, PsiTreeChangeEvent}
import com.intellij.ui.EditorNotifications
import intellij.haskell.HTool.{Hlint, Hoogle, Ormolu, StylishHaskell}
import intellij.haskell.action.HaskellReformatAction
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.editor.HaskellProblemsView
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.{HaskellModuleBuilder, StackProjectImportBuilder}
import intellij.haskell.notification.ConfigFileWatcher
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.stubs.types.HaskellFileElementType
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._
import intellij.haskell.util.index.{HaskellFileIndex, HaskellModuleNameIndex}
import intellij.haskell.{GlobalInfo, HTool, HaskellNotificationGroup}

object StackProjectManager {

  import intellij.haskell.util.ScalaUtil._

  def isInitializing(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.initializing)
  }

  def isHoogleAvailable(project: Project): Option[String] = {
    getStackProjectManager(project).flatMap(_.hoogleAvailable)
  }

  def isHlintAvailable(project: Project): Option[String] = {
    getStackProjectManager(project).flatMap(_.hlintAvailable)
  }

  def isStylishHaskellAvailable(project: Project): Option[String] = {
    getStackProjectManager(project).flatMap(_.stylishHaskellAvailable)
  }

  def isOrmoluAvailable(project: Project): Option[String] = {
    getStackProjectManager(project).flatMap(_.ormoluAvailable)
  }

  def isInstallingHaskellTools(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.installingHaskellTools)
  }

  def isHaddockBuilding(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.haddockBuilding)
  }

  def setHaddockBuilding(project: Project, state: Boolean): Unit = {
    getStackProjectManager(project).foreach(_.haddockBuilding = state)
  }

  def isPreloadingAllLibraryIdentifiers(project: Project): Boolean = {
    getStackProjectManager(project).exists(_.preloadingAllLibraryIdentifiers)
  }

  def stackVersion(project: Project): Option[String] = {
    getStackProjectManager(project).flatMap(_.stackVersion)
  }

  def start(project: Project): Unit = {
    init(project)
  }

  def restart(project: Project): Unit = {
    HaskellFileUtil.saveFiles(project)

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

    ProgressManager.getInstance().run(new Task.Backgroundable(project, title, true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

      private def isToolAvailable(progressIndicator: ProgressIndicator, tool: HTool) = {
        if (HaskellSettingsState.useCustomTools) {
          tool match {
            case Hlint => HaskellSettingsState.hlintPath
            case Hoogle => HaskellSettingsState.hooglePath
            case Ormolu => HaskellSettingsState.ormoluPath
            case StylishHaskell => HaskellSettingsState.stylishHaskellPath
          }
        } else {
          if (!GlobalInfo.toolPath(tool).exists() || update) {
            progressIndicator.setText(s"Busy with installing ${tool.name} in ${GlobalInfo.toolsBinPath}")
            StackCommandLine.installTool(project, progressIndicator, tool.name)
            if (GlobalInfo.toolPath(tool).exists()) {
              Some(GlobalInfo.toolPath(tool).getAbsolutePath)
            } else {
              None
            }
          } else {
            Some(GlobalInfo.toolPath(tool).getAbsolutePath)
          }
        }
      }

      /**
        * Check if at least HLint is installed.
        */
      private def isHLintInstalled: Boolean = {
        GlobalInfo.toolPath(HTool.Hlint).exists()
      }

      override def run(progressIndicator: ProgressIndicator): Unit = {
        try {
          if (update || !isHLintInstalled) {
            progressIndicator.setText(s"Busy with updating Stack's package index")
            StackCommandLine.updateStackIndex(project)
          }

          getStackProjectManager(project).foreach(_.hlintAvailable = isToolAvailable(progressIndicator, HTool.Hlint))

          getStackProjectManager(project).foreach(_.hoogleAvailable = isToolAvailable(progressIndicator, HTool.Hoogle))

          getStackProjectManager(project).foreach(_.stylishHaskellAvailable = isToolAvailable(progressIndicator, HTool.StylishHaskell))

          getStackProjectManager(project).foreach(_.ormoluAvailable = isToolAvailable(progressIndicator, HTool.Ormolu))
        } finally {
          getStackProjectManager(project).foreach(_.installingHaskellTools = false)
        }
      }
    })
  }

  private def init(project: Project, restart: Boolean = false): Unit = {
    if (HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = true)) {
      if (isInitializing(project)) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, "Action not possible whilst project is initializing")
      } else {
        getStackProjectManager(project).foreach(_.initializing = true)
        if (restart) {
          HaskellNotificationGroup.logInfoEvent(project, "Restarting Haskell project")
        } else {
          HaskellNotificationGroup.logInfoEvent(project, "Initializing Haskell project")
        }

        if (getStackProjectManager(project).exists(_.installingHaskellTools == false)) {
          getStackProjectManager(project).foreach(_.hlintAvailable = None)
          getStackProjectManager(project).foreach(_.hoogleAvailable = None)
          getStackProjectManager(project).foreach(_.ormoluAvailable = None)
          getStackProjectManager(project).foreach(_.stylishHaskellAvailable = None)
          installHaskellTools(project, update = false)
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project, starting REPL(s) and preloading cache", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

          def run(progressIndicator: ProgressIndicator): Unit = {
            try {
              progressIndicator.setText("Busy building project's dependencies")
              val dependenciesBuildResult = StackCommandLine.buildProjectDependenciesInMessageView(project)

              if (dependenciesBuildResult.contains(true)) {
                progressIndicator.setText("Busy building project")
                val projectLibTargets = HaskellComponentsManager.findStackComponentInfos(project).filter(_.stanzaType == LibType).map(_.target)
                StackCommandLine.buildInMessageView(project, "Build project libraries", projectLibTargets)
              } else {
                HaskellNotificationGroup.logErrorBalloonEvent(project, "Project won't be built because building its dependencies failed")
              }

              if (restart) {
                val projectRepls = StackReplsManager.getRunningProjectRepls(project)
                progressIndicator.setText("Busy stopping REPLs")
                StackReplsManager.getGlobalRepl(project).foreach(_.exit())
                StackReplsManager.getGlobalRepl2(project).foreach(_.exit())
                projectRepls.foreach(_.exit())

                progressIndicator.setText("Busy cleaning cache")
                HaskellComponentsManager.invalidateGlobalCaches(project)

                ApplicationManager.getApplication.runReadAction(ScalaUtil.runnable {
                  getStackProjectManager(project).foreach(_.initStackReplsManager())
                })

                HaskellProblemsView.getInstance(project).clear()

                progressIndicator.setText("Busy updating project and module settings")
                val projectPath = project.getBasePath
                val projectModules = HaskellProjectUtil.findProjectHaskellModules(project)
                val packagePaths = StackProjectImportBuilder.getPackagePaths(project)
                val packagePathsToAdd = packagePaths.filterNot { relativePath =>
                  val moduleDirectory = HaskellModuleBuilder.getModuleRootDirectory(relativePath, projectPath)
                  projectModules.exists(m => HaskellProjectUtil.getModuleDir(m) == moduleDirectory)
                }

                packagePathsToAdd.foreach(p => {
                  val packagePath = new File(projectPath, p)
                  if (packagePath.exists()) {
                    StackProjectImportBuilder.addHaskellModule(project, p, projectPath)
                  } else {
                    HaskellNotificationGroup.warningEvent(project, s"Couldn't add package $p as module, because its absolute path ${packagePath.getAbsolutePath} doesn't exist.")
                  }
                })

                StackReplsManager.getReplsManager(project).map(_.modulePackageInfos).foreach { moduleCabalInfos =>
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

              val replsLoad = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                StackReplsManager.getReplsManager(project).foreach(_.projectReplTargets.filter(_.stanzaType == LibType).foreach { info =>
                  progressIndicator.setText("Busy starting project REPL " + info.targetsName)
                  StackReplsManager.getProjectRepl(project, info) match {
                    case Some(r) if r.available => HaskellNotificationGroup.logInfoEvent(project, s"REPL ${info.targetsName} is started")
                    case _ => HaskellNotificationGroup.logWarningEvent(project, s"REPL ${info.targetsName} isn't started")
                  }
                  Thread.sleep(1000) // Have to wait between starting the REPLs otherwise timeouts while starting
                })

                val projectFiles = ApplicationUtil.runReadActionWithFileAccess(project,
                  if (project.isDisposed) {
                    Iterable()
                  } else {
                    HaskellFileIndex.findProjectHaskellFiles(project)
                  }, "Finding project files with imported module names")

                val projectFilesWithImportedModuleNames = projectFiles match {
                  case Right(files) => Some(files.map(pf => (pf, ApplicationUtil.runReadAction(HaskellPsiUtil.findImportDeclarations(pf), Some(project)).flatMap(id => ApplicationUtil.runReadAction(id.getModuleName, Some(project))))))
                  case Left(_) =>
                    HaskellNotificationGroup.logInfoEvent(project, "Couldn't retrieve project files")
                    None
                }

                projectFilesWithImportedModuleNames match {
                  case Some(fm) =>
                    fm.foreach { case (pf, moduleNames) =>
                      HaskellPsiUtil.findModuleName(pf).foreach(BrowseModuleComponent.findModuleIdentifiersSync(project, _))

                      moduleNames.foreach(mn => HaskellModuleNameIndex.findFilesByModuleName(project, mn))
                      HaskellNotificationGroup.logInfoEvent(project, "Loading module identifiers " + moduleNames.mkString(", "))
                      moduleNames.foreach(mn => BrowseModuleComponent.findModuleIdentifiersSync(project, mn))
                    }
                  case None => HaskellNotificationGroup.logInfoEvent(project, "Couldn't load module identifiers due to timeout")
                }
              })

              progressIndicator.setText("Busy starting global Stack REPL")
              StackReplsManager.getGlobalRepl(project)

              progressIndicator.setText("Busy starting global Stack REPL2")
              StackReplsManager.getGlobalRepl2(project)

              progressIndicator.setText("Busy preloading global project info")
              GlobalProjectInfoComponent.findGlobalProjectInfo(project)

              progressIndicator.setText("Busy preloading library packages info")
              LibraryPackageInfoComponent.preloadLibraryPackageInfos(project)

              progressIndicator.setText("Busy preloading Stack component info cache")
              val preloadStackComponentInfoCache = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable {
                HaskellComponentsManager.preloadStackComponentInfoCache(project)
              })

              val preloadLibraryFilesCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                if (!project.isDisposed) {
                  progressIndicator.setText("Busy downloading library sources")
                  HaskellModuleBuilder.addLibrarySources(project, update = restart)

                  HaskellComponentsManager.preloadLibraryFilesCache(project)
                }
              })

              progressIndicator.setText("Busy preloading library identifiers")
              val preloadLibraryIdentifiersCacheFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                if (!project.isDisposed) {
                  HaskellComponentsManager.preloadLibraryIdentifiersCaches(project)
                }
              })

              progressIndicator.setText("Busy preloading all library identifiers")
              ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
                if (!project.isDisposed) {
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
                }
              })

              if (!project.isDisposed) {
                val messageBus = project.getMessageBus
                val notifications = EditorNotifications.getInstance(project)
                messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, new ConfigFileWatcher(project, notifications))


                messageBus.connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
                  override def rootsChanged(event: ModuleRootEvent): Unit = {
                    notifications.updateAllNotifications()
                  }
                })

                getStackProjectManager(project).map(_.projectLibraryFileWatcher).foreach { watcher =>
                  messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, watcher)
                }
              }

              HaskellPsiUtil.getPsiManager(project).foreach(_.addPsiTreeChangeListener(new PsiTreeChangeAdapter {

                private def invalidateInfo(event: PsiTreeChangeEvent): Unit = {
                  if (Option(event.getParent).flatMap(p => Option(p.getNode)).exists(_.getElementType != HaskellFileElementType.Instance) || Option(event.getNewChild).isDefined) {
                    Option(event.getFile).foreach(f => {
                      if (Option(event.getNewChild).orElse(Option(event.getParent)).flatMap(HaskellPsiUtil.findImportDeclarations).isDefined) {
                        // Have to refresh because import declarations can be changed
                        FileModuleIdentifiers.refresh(f)
                      } else {
                        val element = Option(event.getNewChild).flatMap(HaskellPsiUtil.findTopDeclarationParent).
                          orElse(Option(event.getParent).flatMap(HaskellPsiUtil.findTopDeclarationParent))
                        val elements = element.map(HaskellPsiUtil.findQualifiedNamedElements).getOrElse(Seq())
                        TypeInfoComponent.invalidateElements(f, elements)
                        NameInfoComponent.invalidateElements(f, elements)
                      }
                    })
                  }
                }

                override def childReplaced(event: PsiTreeChangeEvent): Unit = {
                  invalidateInfo(event)
                }

                override def childrenChanged(event: PsiTreeChangeEvent): Unit = {
                  invalidateInfo(event)
                }

                override def childRemoved(event: PsiTreeChangeEvent): Unit = {
                  invalidateInfo(event)
                }
              }))

              progressIndicator.setText("Busy preloading caches")
              if (!preloadLibraryFilesCacheFuture.isDone || !preloadStackComponentInfoCache.isDone || !preloadLibraryIdentifiersCacheFuture.isDone || !replsLoad.isDone) {
                FutureUtil.waitForValue(project, preloadStackComponentInfoCache, "preloading project cache", 600)
                FutureUtil.waitForValue(project, preloadLibraryFilesCacheFuture, "preloading library files caches", 600)
                FutureUtil.waitForValue(project, preloadLibraryIdentifiersCacheFuture, "preloading library identifiers caches", 600)
                FutureUtil.waitForValue(project, replsLoad, "starting and loading REPLs", 600)
              }
            } finally {
              getStackProjectManager(project).foreach(_.initializing = false)
            }

            // Force-load the module in REPL when REPL can be started. IntelliJ could have wanted to load file (via HaskellAnnotator)
            // but the REPL couldn't be started yet.
            HaskellAnnotator.getNotLoadedFiles(project) foreach { psiFile =>
              HaskellNotificationGroup.logInfoEvent(project, s"${psiFile.getName} will be force-loaded")
              HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
            }

            if (!HoogleComponent.doesHoogleDatabaseExist(project)) {
              HoogleComponent.showHoogleDatabaseDoesNotExistNotification(project)
            }

            StackReplsManager.getReplsManager(project).foreach(_.modulePackageInfos.foreach { case (module, cabalInfo) =>
              val intersection = cabalInfo.sourceRoots.toSeq.intersect(cabalInfo.testSourceRoots.toSeq)
              if (intersection.nonEmpty) {
                intersection.foreach(p => {
                  val moduleName = module.getName
                  HaskellNotificationGroup.logWarningBalloonEvent(project, s"Source folder `$p` of module `$moduleName` is defined both as Source and Test Source")
                })
              }
            })
            HaskellNotificationGroup.logInfoEvent(project, "Finished initializing Haskell project")
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
  private var hoogleAvailable: Option[String] = None

  @volatile
  private var hlintAvailable: Option[String] = None

  @volatile
  private var stylishHaskellAvailable: Option[String] = None

  @volatile
  private var ormoluAvailable: Option[String] = None

  @volatile
  private var installingHaskellTools = false

  @volatile
  private var preloadingAllLibraryIdentifiers = false

  @volatile
  private var haddockBuilding = false

  @volatile
  private var replsManager: Option[StackReplsManager] = None

  private val projectLibraryFileWatcher = new ProjectLibraryFileWatcher(project)

  private lazy val stackVersion = StackCommandLine.stackVersion(project)

  def getStackReplsManager: Option[StackReplsManager] = {
    replsManager
  }

  def initStackReplsManager(): Unit = {
    replsManager = Option(new StackReplsManager(project))
  }

  override def projectClosed(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      replsManager.foreach(_.getGlobalRepl.exit())
      replsManager.foreach(_.getGlobalRepl2.exit())
      replsManager.foreach(_.getRunningProjectRepls.foreach(_.exit()))
      HaskellComponentsManager.invalidateGlobalCaches(project)
    }
  }

  override def initComponent(): Unit = {}

  override def projectOpened(): Unit = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      disableDefaultReformatAction()

      fixSdkStackVersion()

      initStackReplsManager()
      if (replsManager.exists(_.componentTargets.isEmpty)) {
        Messages.showErrorDialog(project, s"Can't start project as no Cabal file was found (or could not be read)", "Can't start project")
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

  // Makes sure that after Stack is updated the right version is displayed.
  private def fixSdkStackVersion(): Unit = {
    val sdks = ProjectJdkTable.getInstance.getSdksOfType(HaskellSdkType.getInstance)
    sdks.forEach { sdk =>
      val sdkModificator = sdk.getSdkModificator
      sdkModificator.setVersionString(HaskellSdkType.getInstance.getVersionString(sdk))
      sdkModificator.commitChanges()
    }
  }
}
