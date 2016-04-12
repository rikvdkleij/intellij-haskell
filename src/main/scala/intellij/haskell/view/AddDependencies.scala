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

package intellij.haskell.view

import java.io.File

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootModificationUtil, OrderRootType}
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.Consumer
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.{ExternalProcess, GhcModProcessManager}
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellProjectUtil

import scala.collection.JavaConversions._

class AddDependencies extends AnAction {

  private final val LibName = "ideaHaskellLib"
  private final val DependencyPattern = """([\w\-]+)\s([\d\.]+)""".r
  private final val InitialProgressStep = 0.1

  override def update(e: AnActionEvent): Unit = e.getPresentation.setEnabledAndVisible(HaskellProjectUtil.isHaskellProject(e.getProject))

  override def actionPerformed(e: AnActionEvent): Unit = {
    HaskellSettingsState.getStackInfo(e.getProject) match {
      case Some(stackInfo) =>
        val project = e.getProject

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell package sources and adding them as source libraries to module") {
          def run(progressIndicator: ProgressIndicator) {
            val libPath = new File(project.getBasePath + File.separator + LibName)
            FileUtil.delete(libPath)
            FileUtil.createDirectory(libPath)
            val dependencyLines = ExternalProcess.getProcessOutput(project.getBasePath, stackInfo.path, Seq("list-dependencies")).getStdoutLines
            val packageInfos = getPackageInfos(dependencyLines)
            progressIndicator.setFraction(InitialProgressStep)
            downloadHaskellPackageSources(project, stackInfo.path, packageInfos, progressIndicator)
            progressIndicator.setFraction(0.9)
            addPackagesAsLibrariesToModule(project, packageInfos, libPath.getAbsolutePath)
            GhcModProcessManager.doRestart(project)
          }
        })
      case None => HaskellNotificationGroup.notifyError("Could not download sources because path to Stack is not set")
    }
  }

  private def getPackageInfos(dependencyLines: Seq[String]) = {
    dependencyLines.flatMap {
      case DependencyPattern(name, version) => Option(HaskellPackageInfo(name, version, s"$name-$version"))
      case x => HaskellNotificationGroup.notifyWarning(s"Could not determine package for line [$x] in output of `stack list-dependencies`"); None
    }
  }

  private def downloadHaskellPackageSources(project: Project, stackPath: String, haskellPackages: Seq[HaskellPackageInfo], progressIndicator: ProgressIndicator) {
    val step = 0.8 / haskellPackages.size
    var progressFraction = InitialProgressStep
    haskellPackages.foreach { p =>
      val fullName = p.name + "-" + p.version
      ExternalProcess.getProcessOutput(project.getBasePath + File.separator + LibName, stackPath, Seq("unpack", fullName))
      progressFraction = progressFraction + step
      progressIndicator.setFraction(progressFraction)
    }
  }

  private def getUrlByPath(path: String): String = {
    VfsUtil.getUrlForLibraryRoot(new File(path))
  }

  private def addPackagesAsLibrariesToModule(project: Project, haskellPackages: Seq[HaskellPackageInfo], libPath: String) {
    ModuleManager.getInstance(project).getModules.headOption match {
      case Some(m) =>
        ModuleRootModificationUtil.updateModel(m, new Consumer[ModifiableRootModel] {
          override def consume(t: ModifiableRootModel): Unit = {
            val libraryTable = t.getModuleLibraryTable
            libraryTable.getLibraries.foreach(l => libraryTable.removeLibrary(l))
            haskellPackages.foreach { hp =>
              val library = libraryTable.createLibrary(hp.name)
              val model = library.getModifiableModel
              model.addRoot(getUrlByPath(libPath + File.separator + hp.fileName), OrderRootType.SOURCES)
              model.commit()
            }
          }
        })
      case None => HaskellNotificationGroup.notifyWarning("Could not add packages as libraries because not Haskell module defined in project")
    }
  }

  private case class HaskellPackageInfo(name: String, version: String, fileName: String)

}
