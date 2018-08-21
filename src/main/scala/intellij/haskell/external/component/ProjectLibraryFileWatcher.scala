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
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.{VFileContentChangeEvent, VFileEvent}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.component.ProjectLibraryFileWatcher.{Build, Building}
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

import scala.collection.JavaConverters._
import scala.collection.concurrent

object ProjectLibraryFileWatcher {

  def isBuilding(project: Project): Boolean = {
    ProjectLibraryFileWatcher.buildStatus.get(project).isDefined
  }

  sealed trait BuildStatus

  case class Building(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  case class Build(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  private val buildStatus: concurrent.Map[Project, BuildStatus] = new ConcurrentHashMap[Project, BuildStatus]().asScala
}

class ProjectLibraryFileWatcher(project: Project) extends BulkFileListener {

  @volatile
  private var currentlyBuildComponents: Set[StackComponentInfo] = Set()

  override def before(events: util.List[_ <: VFileEvent]): Unit = {}

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    if (!project.isDisposed) {
      val infos = for {
        virtualFile <- events.asScala.filter(e => e.isInstanceOf[VFileContentChangeEvent] && HaskellFileUtil.isHaskellFile(e.getFile) && HaskellProjectUtil.isProjectFile(e.getFile, project)).map(_.getFile)
        componentInfo <- HaskellComponentsManager.findStackComponentInfo(project, HaskellFileUtil.getAbsolutePath(virtualFile))
      } yield componentInfo

      val componentLibInfos = infos.toSet.filter(info => info.stanzaType == LibType)
      if (componentLibInfos.nonEmpty) {
        synchronized {
          ProjectLibraryFileWatcher.buildStatus.get(project) match {
            case Some(Building(_)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(componentLibInfos))
            case Some(Build(componentInfos)) => ProjectLibraryFileWatcher.buildStatus.put(project, Build(componentInfos.++(componentLibInfos)))
            case None =>
              ProjectLibraryFileWatcher.buildStatus.put(project, Building(componentLibInfos))
              currentlyBuildComponents = componentLibInfos

              ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building libraries", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

                def run(progressIndicator: ProgressIndicator) {
                  progressIndicator.setText(s"Building: ${currentlyBuildComponents.map(_.target).mkString(", ")}")

                  // Forced `-Wwarn` otherwise build will fail in case of warnings and that will cause that REPLs of dependent targets will not start anymore
                  val output = StackCommandLine.build(project, currentlyBuildComponents.map(_.target).toSeq ++ Seq("--ghc-options", "-Wwarn"), logBuildResult = true)
                  if (output.exists(_.getExitCode == 0) && !project.isDisposed) {
                    val projectRepls = StackReplsManager.getRunningProjectRepls(project)
                    val dependentRepls = projectRepls.filterNot(_.stanzaType == LibType).filter(repl => currentlyBuildComponents.map(_.packageName).contains(repl.packageName))

                    // module name == package name
                    val dependentModules = HaskellProjectUtil.findProjectModules(project).filter(m => currentlyBuildComponents.exists(ci => ModuleRootManager.getInstance(m).getDependencyModuleNames.contains(ci.packageName))).toSeq

                    val dependentLibRepls = projectRepls.filter(repl => repl.stanzaType == LibType && dependentModules.map(_.getName).contains(repl.packageName))

                    val openFiles = FileEditorManager.getInstance(project).getOpenFiles.filter(HaskellFileUtil.isHaskellFile)
                    val openProjectFiles = openFiles.filter(f => HaskellProjectUtil.isProjectFile(f, project))
                    val dependentFiles = openProjectFiles.filter(f => HaskellComponentsManager.findStackComponentInfo(project, HaskellFileUtil.getAbsolutePath(f)).exists(ci => ci.stanzaType != LibType && currentlyBuildComponents.map(_.packageName).contains(ci.packageName)))
                    val dependentLibFiles = openProjectFiles.toSeq.diff(dependentFiles.toSeq).filter(f => HaskellProjectUtil.findModuleForVirtualFile(project, f).exists(m => dependentModules.contains(m)))

                    (dependentRepls ++ dependentLibRepls).foreach(_.restart())

                    import scala.concurrent.duration._

                    (dependentFiles ++ dependentLibFiles).foreach { vf =>
                      HaskellFileUtil.convertToHaskellFileInReadAction(project, vf, timeout = 1.second).toOption.flatten match {
                        case Some(psiFile) =>
                          HaskellComponentsManager.invalidateCachesForFile(psiFile)
                          HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
                        case None => HaskellNotificationGroup.logInfoEvent(project, s"Could not invalidate cache and restart daemon analyzer for file ${vf.getName}")
                      }
                    }
                  }

                  if (!project.isDisposed) {
                    val buildStatus = ProjectLibraryFileWatcher.buildStatus.get(project)
                    buildStatus match {
                      case Some(Build(componentInfos)) =>
                        currentlyBuildComponents = componentInfos
                        ProjectLibraryFileWatcher.buildStatus.put(project, Building(componentInfos))
                        run(progressIndicator)
                      case _ =>
                        currentlyBuildComponents = Set()
                        ProjectLibraryFileWatcher.buildStatus.remove(project)
                    }
                  }
                }
              })
          }
        }
      }
    }

  }
}