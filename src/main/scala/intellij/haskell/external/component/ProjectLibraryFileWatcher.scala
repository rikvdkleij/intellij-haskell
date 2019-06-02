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

import java.util
import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.{VFileContentChangeEvent, VFileEvent}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.component.ProjectLibraryFileWatcher.{Build, Building}
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil, HaskellProjectUtil}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.concurrent

object ProjectLibraryFileWatcher {

  def isBuilding(project: Project): Boolean = {
    StackProjectManager.getProjectLibraryFileWatcher(project).exists(_.currentlyBuildLibComponents.nonEmpty)
  }

  sealed trait BuildStatus

  case class Building(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  case class Build(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  private val buildStatus: concurrent.Map[Project, BuildStatus] = new ConcurrentHashMap[Project, BuildStatus]().asScala

  def addBuild(project: Project, libComponentInfos: Set[StackComponentInfo]): Option[BuildStatus] = {
    synchronized {
      ProjectLibraryFileWatcher.buildStatus.get(project) match {
        case Some(Building(_)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(libComponentInfos))
        case Some(Build(componentInfos)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(componentInfos.++(libComponentInfos)))
        case None => ProjectLibraryFileWatcher.buildStatus.put(project, Build(libComponentInfos))
      }
    }
  }

  def checkLibraryBuild(project: Project, currentInfo: StackComponentInfo): Unit = synchronized {
    if (!StackProjectManager.isInitializing(project) && !StackProjectManager.isHaddockBuilding(project) && !project.isDisposed) {
      ProjectLibraryFileWatcher.buildStatus.get(project) match {
        case Some(Build(infos)) if !isBuilding(project) && infos.exists(_ != currentInfo) => build(project, infos)
        case _ => ()
      }
    }
  }

  private def build(project: Project, libComponentInfos: Set[StackComponentInfo]): Unit = {
    StackProjectManager.getProjectLibraryFileWatcher(project).foreach { watcher =>
      watcher.currentlyBuildLibComponents = libComponentInfos
      ProjectLibraryFileWatcher.buildStatus.put(project, Building(libComponentInfos))

      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

        @tailrec
        def run(progressIndicator: ProgressIndicator) {
          val buildMessage = s"Building project"
          HaskellNotificationGroup.logInfoEvent(project, buildMessage)
          progressIndicator.setText(buildMessage)

          // Forced `-Wwarn` otherwise build will fail in case of warnings and that will cause that REPLs of dependent targets will not start anymore
          val projectLibTargets = HaskellComponentsManager.findStackComponentInfos(project).filter(_.stanzaType == LibType).map(_.target)
          val output = StackCommandLine.buildProjectInMessageView(project, projectLibTargets ++ Seq("--ghc-options", "-Wwarn"))
          if (output.contains(true) && !project.isDisposed) {
            val projectRepls = StackReplsManager.getRunningProjectRepls(project)
            val openFiles = FileEditorManager.getInstance(project).getOpenFiles.filter(HaskellFileUtil.isHaskellFile)
            val openProjectFiles = openFiles.filter(vf => HaskellProjectUtil.isSourceFile(project, vf))
            val openInfoFiles = openProjectFiles.flatMap(f =>
              HaskellComponentsManager.findStackComponentInfo(project, HaskellFileUtil.getAbsolutePath(f)) match {
                case Some(i) => Some((i, f))
                case None => None
              })

            val isDependentResult = libComponentInfos.map(libInfo => {
              val module = libInfo.module
              val dependentModules = ApplicationUtil.runReadAction(ModuleUtilCore.getAllDependentModules(module))

              val dependentFiles = openInfoFiles.filter { case (info, _) => isDependent(libInfo, dependentModules, info) }.map(_._2)
              val dependentRepls = projectRepls.filter(r => isDependent(libInfo, dependentModules, r.stackComponentInfo))
              (dependentFiles, dependentRepls)
            })

            val dependentFiles = isDependentResult.flatMap(_._1)
            val dependentRepls = isDependentResult.flatMap(_._2)

            dependentRepls.foreach { repl =>
              repl.restart()
            }

            // When project is opened and has build errors some REPLs could not have been started
            StackReplsManager.getReplsManager(project).foreach(_.stackComponentInfos.filter(_.stanzaType == LibType).foreach { info =>
              StackReplsManager.getProjectRepl(project, info).foreach { repl =>
                if (!repl.available && !repl.starting) {
                  repl.start()
                }
              }
            })

            HaskellComponentsManager.invalidateBrowseInfo(project, libComponentInfos.flatMap(_.exposedModuleNames).toSeq)

            dependentFiles.foreach { vf =>
              HaskellFileUtil.convertToHaskellFileInReadAction(project, vf).toOption match {
                case Some(psiFile) =>
                  HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
                case None => HaskellNotificationGroup.logInfoEvent(project, s"Could not invalidate cache and restart daemon analyzer for file ${vf.getName}")
              }
            }
          }

          if (!project.isDisposed) {
            val buildStatus = ProjectLibraryFileWatcher.buildStatus.get(project)
            buildStatus match {
              case Some(Build(componentInfos)) =>
                watcher.currentlyBuildLibComponents = componentInfos
                ProjectLibraryFileWatcher.buildStatus.put(project, Building(componentInfos))
                run(progressIndicator)
              case _ =>
                watcher.currentlyBuildLibComponents = Set()
                ProjectLibraryFileWatcher.buildStatus.remove(project)
            }
          }
        }
      })
    }
  }

  private def isDependent(libInfo: StackComponentInfo, dependentModules: util.List[Module], info: StackComponentInfo) = {
    (info.module == libInfo.module && info.stanzaType != LibType) || dependentModules.contains(info.module)
  }
}

class ProjectLibraryFileWatcher(project: Project) extends BulkFileListener {

  @volatile
  private var currentlyBuildLibComponents: Set[StackComponentInfo] = Set()

  override def before(events: util.List[_ <: VFileEvent]): Unit = {}

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    if (!project.isDisposed) {
      val libComponentInfos = (for {
        virtualFile <- events.asScala.filter(e => e.isInstanceOf[VFileContentChangeEvent] && HaskellFileUtil.isHaskellFile(e.getFile) && HaskellProjectUtil.isSourceFile(project, e.getFile)).map(_.getFile)
        componentInfo <- HaskellComponentsManager.findStackComponentInfo(project, HaskellFileUtil.getAbsolutePath(virtualFile))
        if componentInfo.stanzaType == LibType
      } yield componentInfo).toSet

      if (libComponentInfos.nonEmpty) {
        synchronized {
          ProjectLibraryFileWatcher.buildStatus.get(project) match {
            case Some(Building(_)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(libComponentInfos))
            case Some(Build(componentInfos)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(componentInfos.++(libComponentInfos)))
            case None => ProjectLibraryFileWatcher.buildStatus.put(project, Build(libComponentInfos))
          }
        }
      }
    }
  }
}