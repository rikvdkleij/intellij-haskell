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
import intellij.haskell.external.{GhcModProcessManager, ExternalProcess}
import intellij.haskell.settings.{CabalInfo, HaskellSettingsState}
import intellij.haskell.util.HaskellProjecUtil

import scala.collection.JavaConversions._

class AddDependencies extends AnAction {

  private val libName = "ideaHaskellLib"
  private val PackageInCabalConfigPattern = """.* ([\w\-]+)\s*==\s*([\d\.]+),?""".r
  private val initialProgressStep = 0.1

  override def update(e: AnActionEvent): Unit = e.getPresentation.setEnabledAndVisible(HaskellProjecUtil.isHaskellProject(e.getProject))

  override def actionPerformed(e: AnActionEvent): Unit = {
    HaskellSettingsState.getCabalInfo(e.getProject) match {
      case Some(cabalInfo) =>
        val project = e.getProject

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell package sources and adding them as source libraries to module") {
          def run(progressIndicator: ProgressIndicator) {
            val libPath = new File(project.getBasePath + File.separator + libName)
            FileUtil.delete(libPath)
            FileUtil.createDirectory(libPath)
            ExternalProcess.getProcessOutput(project.getBasePath, cabalInfo.path, getCabalFreezeArguments(cabalInfo))
            readCabalConfig(project, cabalInfo.path).map(cl => getHaskellPackages(cl)).foreach(packages => {
              progressIndicator.setFraction(initialProgressStep)
              downloadHaskellPackageSources(project, cabalInfo.path, packages, progressIndicator)
              progressIndicator.setFraction(0.9)
              addDependenciesAsLibrariesToModule(project, packages, libPath.getAbsolutePath)
            })
            GhcModProcessManager.doRestart(project)
          }
        })
      case None => HaskellNotificationGroup.notifyError("Could not download sources because path to Cabal is not set")
    }
  }

  private def getCabalFreezeArguments(cabalInfo: CabalInfo) = {
    Seq("freeze") ++ (
      if (cabalInfo.version > "1.22") {
        Seq("--enable-tests")
      } else {
        Seq()
      })
  }

  private def readCabalConfig(project: Project, cabalPath: String): Option[Seq[String]] = {
    try {
      Option(FileUtil.loadLines(project.getBasePath + File.separator + "cabal.config"))
    } catch {
      case e: Exception =>
        HaskellNotificationGroup.notifyError(s"Could not read cabal.config file. Error: ${e.getMessage}")
        None
    }
  }

  private def getHaskellPackages(cabalConfigLines: Seq[String]) = {
    cabalConfigLines.flatMap {
      case PackageInCabalConfigPattern(name, version) => Option(HaskellPackage(name, version, s"$name-$version"))
      case x => HaskellNotificationGroup.notifyWarning(s"Could not determine package for line [$x] in cabal.config file"); None
    }
  }

  private def downloadHaskellPackageSources(project: Project, cabalPath: String, haskellPackages: Seq[HaskellPackage], progressIndicator: ProgressIndicator) {
    val step = 0.8 / haskellPackages.size
    var progressFraction = initialProgressStep
    haskellPackages.foreach { p =>
      val fullName = p.name + "-" + p.version
      ExternalProcess.getProcessOutput(project.getBasePath, cabalPath, Seq("get", "-d", libName, fullName))
      ExternalProcess.getProcessOutput(project.getBasePath, cabalPath, Seq("sandbox", "add-source", libName + File.separator + fullName))
      progressFraction = progressFraction + step
      progressIndicator.setFraction(progressFraction)
    }
  }

  private def getUrlByPath(path: String): String = {
    VfsUtil.getUrlForLibraryRoot(new File(path))
  }

  private def addDependenciesAsLibrariesToModule(project: Project, haskellPackages: Seq[HaskellPackage], libPath: String) {
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

  case class HaskellPackage(name: String, version: String, fileName: String)

}
