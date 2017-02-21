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

package intellij.haskell.module

import java.io.File
import javax.swing.Icon

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.{Module, ModuleType}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootManager, ModuleRootModificationUtil, OrderRootType}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VfsUtil}
import intellij.haskell.external.commandLine.{CommandLine, StackCommandLine}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.HaskellProjectUtil
import intellij.haskell.{HaskellIcons, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class HaskellModuleBuilder extends ModuleBuilder with SourcePathsBuilder with ModuleBuilderListener {

  private[this] val sourcePaths = new java.util.ArrayList[Pair[String, String]]()

  private[this] var isNewProject = false

  override def moduleCreated(module: Module): Unit = {
    HaskellModuleBuilder.addLibrarySources(module)
  }

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = HaskellModuleType.getInstance

  override def isSuitableSdkType(sdkType: SdkTypeId): Boolean = {
    sdkType == HaskellSdkType.getInstance
  }

  override def getNodeIcon: Icon = HaskellIcons.HaskellSmallLogo

  override def setupRootModel(rootModel: ModifiableRootModel): Unit = {
    addListener(this)

    if (rootModel.getSdk == null) {
      rootModel.setSdk(HaskellSdkType.findOrCreateSdk())
      rootModel.inheritSdk()
    }

    val contentEntry = doAddContentEntry(rootModel)

    if (contentEntry != null) {
      getSourcePaths.asScala.foreach { path =>
        val folder = Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path.first)))
        folder.foreach { f =>
          contentEntry.addSourceFolder(f, false, path.second)
        }
      }

      getTestSourcePath.foreach { path =>
        val folder = Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path)))
        folder.foreach { f =>
          contentEntry.addSourceFolder(f, true, path)
        }
      }

      val stackWorkDirectory = getStackWorkDirectory
      stackWorkDirectory.mkdir()
      Option(LocalFileSystem.getInstance.refreshAndFindFileByIoFile(stackWorkDirectory)).foreach { f =>
        contentEntry.addExcludeFolder(f)
      }

      val libraryDirectory = HaskellModuleBuilder.getIdeaHaskellLibDirectory(rootModel.getProject)
      libraryDirectory.mkdir()
      Option(LocalFileSystem.getInstance.refreshAndFindFileByIoFile(libraryDirectory)).foreach(f => {
        contentEntry.addExcludeFolder(f)
      })
    }
  }

  override def setupModule(module: Module): Unit = {
    if (isNewProject) {
      val configurationUpdater = new ModuleBuilder.ModuleConfigurationUpdater {

        override def update(module: Module, rootModel: ModifiableRootModel): Unit = {
          val project = rootModel.getProject
          StackCommandLine.runCommand(
            Seq("new", project.getName, "--bare", "hspec"),
            project,
            timeoutInMillis = 20.seconds.toMillis
          )
        }
      }
      val modifiableModel: ModifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel
      configurationUpdater.update(module, modifiableModel)
    }
    super.setupModule(module)
  }

  override def setSourcePaths(sourcePaths: java.util.List[Pair[String, String]]): Unit = {
    sourcePaths.clear()
    sourcePaths.addAll(sourcePaths)
  }

  override def addSourcePath(sourcePathInfo: Pair[String, String]): Unit = {
    sourcePaths.add(sourcePathInfo)
  }

  override def getSourcePaths: java.util.List[Pair[String, String]] = {
    if (sourcePaths.isEmpty) {
      val paths = new java.util.ArrayList[Pair[String, String]]
      Seq("src", "lib", "app", "library", "main").foreach { dirName =>
        val path = getContentEntryPath + File.separator + dirName
        val srcDir = new File(path)
        if (srcDir.isDirectory) {
          paths.add(Pair.create(path, ""))
        }
      }
      paths
    } else {
      sourcePaths
    }
  }

  // Only called in case new project
  override def getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep = {
    isNewProject = true
    new HaskellModuleWizardStep(context, this)
  }

  private def getStackWorkDirectory = {
    new File(getContentEntryPath, ".stack-work")
  }

  private def getTestSourcePath = {
    getExistingPath("test")
  }

  private def getExistingPath(dirName: String) = {
    val path = new File(getContentEntryPath, dirName)
    if (path.isDirectory) {
      Some(path.getAbsolutePath)
    } else {
      None
    }
  }
}

class HaskellModuleWizardStep(wizardContext: WizardContext, haskellModuleBuilder: HaskellModuleBuilder) extends ProjectJdkForModuleStep(wizardContext, HaskellSdkType.getInstance) {

  override def updateDataModel() {
    super.updateDataModel()
    haskellModuleBuilder.setModuleJdk(getJdk)
  }

  override def validate(): Boolean = {
    if (getJdk == null) {
      Messages.showErrorDialog("You can not create Haskell project without Stack configured as SDK", "No Haskell Tool Stack specified")
      false
    } else {
      true
    }
  }
}

object HaskellModuleBuilder {

  private final val LibName = ".ideaHaskellLib"
  private final val DependencyPattern = """([\w\-]+)\s([\d\.]+)""".r
  private final val InitialProgressStep = 0.1
  private final val GhcPrimVersion = "0.5.0.0"
  private final val GhcPrim = "ghc-prim"

  def addLibrarySources(module: Module): Unit = {
    val project = module.getProject
    HaskellSdkType.getStackPath(project).foreach(stackPath => {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell library sources and adding them as source libraries to module") {
        def run(progressIndicator: ProgressIndicator) {
          val libDirectory = getIdeaHaskellLibDirectory(project)
          FileUtil.delete(libDirectory)
          FileUtil.createDirectory(libDirectory)
          StackCommandLine.runCommand(Seq("list-dependencies", "--test"), project, timeoutInMillis = 60.seconds.toMillis).map(_.getStdoutLines).foreach(dependencyLines => {
            val packageName = HaskellProjectUtil.findCabalPackageName(project)
            val packages = getPackages(project, dependencyLines.asScala).filterNot(p => packageName.contains(p.name))
            progressIndicator.setFraction(InitialProgressStep)
            val downloadedPackages = downloadHaskellPackageSources(project, stackPath, packages, progressIndicator)
            progressIndicator.setFraction(0.9)
            addPackagesAsLibrariesToModule(module, downloadedPackages, libDirectory.getAbsolutePath)
          })
        }
      })
    })
  }

  def getIdeaHaskellLibDirectory(project: Project): File = {
    new File(project.getBasePath, LibName)
  }

  private def getPackages(project: Project, dependencyLines: Seq[String]): Seq[HaskellPackageInfo] = {
    val packageInfos = dependencyLines.flatMap {
      case DependencyPattern(name, version) => Option(HaskellPackageInfo(name, version, s"$name-$version"))
      case x => HaskellNotificationGroup.logWarningEvent(project, s"Could not determine package for line [$x] in output of `stack list-dependencies --test`"); None
    }

    if (packageInfos.exists(_.name == GhcPrim)) {
      packageInfos
    } else {
      packageInfos ++ Seq(HaskellPackageInfo(GhcPrim, GhcPrimVersion, s"$GhcPrim-$GhcPrimVersion"))
    }
  }

  private def downloadHaskellPackageSources(project: Project, stackPath: String, haskellPackages: Seq[HaskellPackageInfo], progressIndicator: ProgressIndicator) = {
    val step = 0.8 / haskellPackages.size
    var progressFraction = InitialProgressStep
    haskellPackages.flatMap { packageInfo =>
      val fullName = packageInfo.name + "-" + packageInfo.version
      val stdErr = CommandLine.runProgram(Some(project), project.getBasePath + File.separator + LibName, stackPath, Seq("unpack", fullName), 10000, captureOutputToLog = true, logErrorAsInfo = true).map(_.getStderr)
      progressFraction = progressFraction + step
      progressIndicator.setFraction(progressFraction)

      if (stdErr.exists(_.contains("not found"))) {
        Seq()
      } else {
        Seq(packageInfo)
      }
    }
  }

  private def getUrlByPath(path: String): String = {
    VfsUtil.getUrlForLibraryRoot(new File(path))
  }

  private def addPackagesAsLibrariesToModule(module: Module, haskellPackages: Seq[HaskellPackageInfo], libPath: String) {
    ModuleRootModificationUtil.updateModel(module, (t: ModifiableRootModel) => {
      val libraryTable = t.getModuleLibraryTable
      libraryTable.getLibraries.foreach(l => libraryTable.removeLibrary(l))
      haskellPackages.foreach { hp =>
        val library = libraryTable.createLibrary(hp.name)
        val model = library.getModifiableModel
        model.addRoot(getUrlByPath(libPath + File.separator + hp.dirName), OrderRootType.SOURCES)
        model.commit()
      }
    })
  }

  private case class HaskellPackageInfo(name: String, version: String, dirName: String)

}
