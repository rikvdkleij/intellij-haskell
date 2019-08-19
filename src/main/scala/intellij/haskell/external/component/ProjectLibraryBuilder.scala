/*
 * Copyright 2014-2019 Rik van der Kleij
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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil, HaskellProjectUtil}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

object ProjectLibraryBuilder {

  private val buildStatus = new ConcurrentHashMap[Project, BuildStatus].asScala

  def isBuilding(project: Project): Boolean = {
    buildStatus.get(project).exists {
      case Building(_) => true
      case _ => false
    }
  }

  def resetBuildStatus(project: Project): Option[BuildStatus] = {
    buildStatus.remove(project)
  }

  sealed trait BuildStatus

  case class Building(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  case class Build(stackComponentInfos: Set[StackComponentInfo]) extends BuildStatus

  def addBuild(project: Project, libComponentInfos: Set[StackComponentInfo]): Option[BuildStatus] = {
    synchronized {
      buildStatus.get(project) match {
        case Some(Building(_)) => buildStatus.put(project, Build(libComponentInfos))
        case Some(Build(componentInfos)) => buildStatus.put(project, Build(componentInfos.++(libComponentInfos)))
        case None => buildStatus.put(project, Build(libComponentInfos))
      }
    }
  }

  def checkLibraryBuild(project: Project, currentInfo: StackComponentInfo): Unit = synchronized {
    if (!StackProjectManager.isInitializing(project) && !StackProjectManager.isHaddockBuilding(project) && !project.isDisposed) {
      buildStatus.get(project) match {
        case Some(Build(infos)) if !isBuilding(project) && infos.exists(_ != currentInfo) => build(project, infos)
        case _ => ()
      }
    }
  }

  private def build(project: Project, libComponentInfos: Set[StackComponentInfo]): Unit = {
    buildStatus.put(project, Building(libComponentInfos))

    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

      @tailrec
      def run(progressIndicator: ProgressIndicator): Unit = {

        // Forced `-Wwarn` otherwise build will fail in case of warnings and that will cause that REPLs of dependent targets will not start anymore
        val projectLibTargets = HaskellComponentsManager.findStackComponentInfos(project).filter(_.stanzaType == LibType).map(_.target)

        val buildMessage = s"Building targets: " + projectLibTargets.mkString(", ")
        HaskellNotificationGroup.logInfoEvent(project, buildMessage)
        progressIndicator.setText(buildMessage)

        val output = StackCommandLine.buildInBackground(project, projectLibTargets ++ Seq("--ghc-options", "-Wwarn"))
        if (output.contains(true) && !project.isDisposed) {
          val projectRepls = StackReplsManager.getRunningProjectRepls(project)
          val openFiles = FileEditorManager.getInstance(project).getOpenFiles.filter(HaskellFileUtil.isHaskellFile)
          val openProjectFiles = openFiles.filter(vf => HaskellProjectUtil.isSourceFile(project, vf))
          val openInfoFiles = openProjectFiles.toSeq.flatMap(f =>
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
          buildStatus.get(project) match {
            case Some(Build(componentInfos)) =>
              buildStatus.put(project, Building(componentInfos))
              run(progressIndicator)
            case _ =>
              buildStatus.remove(project)
          }
        }
      }
    })
  }

  private def isDependent(libInfo: StackComponentInfo, dependentModules: util.List[Module], info: StackComponentInfo) = {
    (info.module == libInfo.module && info.stanzaType != LibType) || dependentModules.contains(info.module)
  }
}
