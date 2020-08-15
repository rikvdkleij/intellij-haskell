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

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.editor.HaskellProblemsView
import intellij.haskell.external.component.HaskellComponentsManager.ComponentTarget
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.external.repl.StackReplsManager.ProjectReplTargets
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

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

  case class Building(stackComponentInfos: Set[ComponentTarget]) extends BuildStatus

  case class Build(stackComponentInfos: Set[ComponentTarget]) extends BuildStatus

  def addBuild(project: Project, componentTargets: Set[ComponentTarget]): Option[BuildStatus] = synchronized {
    buildStatus.get(project) match {
      case Some(Building(_)) => buildStatus.put(project, Build(componentTargets))
      case Some(Build(targets)) => buildStatus.put(project, Build(targets ++ componentTargets))
      case None => buildStatus.put(project, Build(componentTargets))
    }
  }

  def checkLibraryBuild(project: Project, currentTargets: ProjectReplTargets): Unit = synchronized {
    if (!StackProjectManager.isInitializing(project) && !StackProjectManager.isHaddockBuilding(project) && !project.isDisposed) {
      val libTargetsName = StackReplsManager.getReplsManager(project).flatMap(_.libTargetsName)
      (buildStatus.get(project), libTargetsName) match {
        case (Some(Build(targets)), Some(libTargetsName)) if currentTargets.stanzaType != LibType && !isBuilding(project) => build(project, targets, libTargetsName)
        case _ => ()
      }
    }
  }

  private def build(project: Project, componentLibTargets: Set[ComponentTarget], libTargetsName: String): Unit = {
    buildStatus.put(project, Building(componentLibTargets))

    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

      def run(progressIndicator: ProgressIndicator): Unit = {

        val buildMessage = s"Building targets: " + libTargetsName
        HaskellNotificationGroup.logInfoEvent(project, buildMessage)
        progressIndicator.setText(buildMessage)

        // Forced `-Wwarn` otherwise build will fail in case of warnings and that will cause that REPLs of dependent targets will not start anymore
        val buildResult = StackCommandLine.buildInBackground(project, Seq(libTargetsName, "--ghc-options", "-Wwarn"))
        if (buildResult.contains(true) && !project.isDisposed) {
          val openFiles = FileEditorManager.getInstance(project).getOpenFiles.filter(HaskellFileUtil.isHaskellFile)
          val openProjectFiles = openFiles.filter(vf => HaskellProjectUtil.isSourceFile(project, vf))
          val openNonLibFiles = openProjectFiles.flatMap(file =>
            HaskellComponentsManager.findComponentTarget(project, HaskellFileUtil.getAbsolutePath(file)) match {
              case Some(target) => Some((target, file))
              case None => None
            }).filter(_._1.stanzaType != LibType)

          val projectNonLibRepls = StackReplsManager.getRunningProjectRepls(project).filter(_.stanzaType != LibType)
          projectNonLibRepls.foreach { repl =>
            repl.restart()
          }

          // When project is opened and has build errors some REPLs could not have been started
          StackReplsManager.getReplsManager(project).foreach(_.projectReplTargets.filter(_.stanzaType == LibType).foreach { info =>
            StackReplsManager.getProjectRepl(project, info).foreach { repl =>
              if (!repl.available && !repl.starting) {
                repl.start()
              }
            }
          })

          HaskellComponentsManager.invalidateBrowseInfo(project, componentLibTargets.flatMap(_.exposedModuleNames).toSeq)
          componentLibTargets.foreach(target => target.exposedModuleNames.foreach(FileModuleIdentifiers.invalidate))

          openNonLibFiles.map(_._2).foreach { vf =>
            HaskellFileUtil.convertToHaskellFileInReadAction(project, vf).toOption match {
              case Some(psiFile) =>
                val haskellProblemsView = HaskellProblemsView.getInstance(project)
                HaskellFileUtil.findVirtualFile(psiFile).foreach(haskellProblemsView.clearOldMessages)
                HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
              case None => HaskellNotificationGroup.logInfoEvent(project, s"Could not invalidate cache and restart daemon analyzer for file ${vf.getName}")
            }
          }
        }
        buildStatus.remove(project)
      }
    })
  }
}
