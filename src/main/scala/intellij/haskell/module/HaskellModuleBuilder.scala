/*
 * Copyright 2014-2017 Rik van der Kleij
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
import com.intellij.openapi.application.{Result, WriteAction}
import com.intellij.openapi.module.{Module, ModuleType}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots._
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VfsUtil}
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.execution.{CaptureOutputToLog, CommandLine, StackCommandLine}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.HaskellProjectUtil
import intellij.haskell.{HaskellIcons, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class HaskellModuleBuilder extends ModuleBuilder {

  private var cabalInfo: CabalInfo = _

  def setCabalInfo(cabalInfo: CabalInfo): Unit = {
    this.cabalInfo = cabalInfo
  }

  private[this] var isNewProjectWithoutExistingSources = false

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = HaskellModuleType.getInstance

  override def isSuitableSdkType(sdkType: SdkTypeId): Boolean = {
    sdkType == HaskellSdkType.getInstance
  }

  override def getNodeIcon: Icon = HaskellIcons.HaskellSmallLogo

  override def setupRootModel(rootModel: ModifiableRootModel): Unit = {
    if (rootModel.getSdk == null) {
      rootModel.setSdk(HaskellSdkType.findOrCreateSdk())
      rootModel.inheritSdk()
    }

    val contentEntry = doAddContentEntry(rootModel)
    val project = rootModel.getProject

    if (isNewProjectWithoutExistingSources) {
      val packagePath = StackYamlComponent.getPackagePaths(project).flatMap(_.headOption)
      packagePath.flatMap(pp => HaskellModuleBuilder.createCabalInfo(rootModel.getProject, project.getBaseDir.getPath, pp)) match {
        case Some(ci) => cabalInfo = ci
        case None =>
          Messages.showErrorDialog(s"Could not create Haskell project because can not retrieve info from Cabal file for package path $packagePath", "No Cabal file info")
      }
    }

    if (contentEntry != null) {
      cabalInfo.getSourceRoots.foreach(path => {
        Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))).foreach(f =>
          contentEntry.addSourceFolder(f, false)
        )
      })

      cabalInfo.getTestSourceRoots.foreach(path => {
        Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))).foreach(f =>
          contentEntry.addSourceFolder(f, true)
        )
      })

      val stackWorkDirectory = getStackWorkDirectory
      stackWorkDirectory.mkdir()
      Option(LocalFileSystem.getInstance.refreshAndFindFileByIoFile(stackWorkDirectory)).foreach(f =>
        contentEntry.addExcludeFolder(f)
      )
    }
  }

  override def setupModule(module: Module): Unit = {
    if (isNewProjectWithoutExistingSources) {
      val configurationUpdater = new ModuleBuilder.ModuleConfigurationUpdater {

        override def update(module: Module, rootModel: ModifiableRootModel): Unit = {
          val project = rootModel.getProject
          StackCommandLine.runCommand(project, Seq("new", project.getName, "--bare", "hspec"), timeoutInMillis = 20.seconds.toMillis)
        }
      }
      val modifiableModel: ModifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel
      configurationUpdater.update(module, modifiableModel)
    }
    super.setupModule(module)
  }

  // Only called in case new project without existing Stack project
  override def getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep = {
    isNewProjectWithoutExistingSources = true
    new HaskellModuleWizardStep(context, this)
  }

  private def getStackWorkDirectory = {
    new File(getContentEntryPath, ".stack-work")
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

  private final val IdeaHaskellLibName = ".intellij-haskell" + File.separator + "lib"
  private final val DependencyPattern = """([\w\-]+)\s([\d\.]+)""".r
  private final val InitialProgressStep = 0.1
  private final val GhcPrimVersion = "0.5.0.0"
  private final val GhcPrim = "ghc-prim"

  def createCabalInfo(project: Project, modulePath: String, packagePath: String): Option[CabalInfo] = {
    val moduleDirectory = getModuleRootDirectory(packagePath, modulePath)
    for {
      cabalFile <- getCabalFile(moduleDirectory)
      cabalInfo <- getCabalInfo(project, cabalFile)
    } yield cabalInfo
  }

  private def getModuleRootDirectory(packagePath: String, modulePath: String): File = {
    if (packagePath == ".") {
      new File(modulePath)
    } else {
      new File(modulePath, packagePath)
    }
  }

  private def getCabalFile(moduleDirectory: File): Option[File] = {
    HaskellProjectUtil.findCabalFile(moduleDirectory) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(s"Can not create Haskell module because Cabal file can not be found in $moduleDirectory", "Can not create Haskell module")
        None
    }
  }

  private def getCabalInfo(project: Project, cabalFile: File): Option[CabalInfo] = {
    CabalInfo.create(project, cabalFile) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(project, s"Can not create Haskell module because Cabal file $cabalFile can not be read", "Can not create Haskell module")
        None
    }
  }

  def addLibrarySources(module: Module): Unit = {
    val project = module.getProject

    new WriteAction[Unit]() {

      override protected def run(result: Result[Unit]): Unit = {
        val projectLibraryTable = ProjectLibraryTable.getInstance(module.getProject)
        projectLibraryTable.getModifiableModel.getLibraries.foreach(projectLibraryTable.removeLibrary)
      }
    }

    HaskellSdkType.getStackPath(project).foreach(stackPath => {
      val packageName = module.getName

      removeOldIdeaHaskellLibDirectory(project)

      val projectLibDirectory = getProjectLibDirectory(project)
      if (!projectLibDirectory.exists()) {
        FileUtil.createDirectory(projectLibDirectory)
      }

      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell library sources and adding them as source libraries to module") {

        def run(progressIndicator: ProgressIndicator) {
          StackCommandLine.runCommand(project, Seq("list-dependencies", packageName, "--test", "--bench"), timeoutInMillis = 60.seconds.toMillis).map(_.getStdoutLines).foreach(dependencyLines => {
            val packages = getPackages(project, dependencyLines.asScala).filterNot(p => packageName.contains(p.name) || p.name == "rts")
            progressIndicator.setFraction(InitialProgressStep)
            val downloadedPackages = downloadHaskellPackageSources(project, projectLibDirectory, stackPath, packages, progressIndicator)
            progressIndicator.setFraction(0.9)
            addPackagesAsLibrariesToModule(module, downloadedPackages, projectLibDirectory)
          })
        }
      })
    })
  }

  private def removeOldIdeaHaskellLibDirectory(project: Project): Unit = {
    val oldLibDir = new File(project.getBasePath, ".ideaHaskellLib")
    if (oldLibDir.exists()) {
      FileUtil.delete(oldLibDir)
    }
  }

  private def getProjectLibDirectory(project: Project): File = {
    val homeDirectory = VfsUtil.getUserHomeDir.getPath
    new File(new File(homeDirectory, IdeaHaskellLibName), project.getName)
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

  private def downloadHaskellPackageSources(project: Project, projectLibDirectory: File, stackPath: String, haskellPackages: Seq[HaskellPackageInfo], progressIndicator: ProgressIndicator) = {
    val step = 0.8 / haskellPackages.size
    var progressFraction = InitialProgressStep

    val alreadyDownloadedPackages = haskellPackages.filter(packageInfo => {
      getPackageDirectory(projectLibDirectory, packageInfo).exists()
    })
    val downloadedPackages = haskellPackages.filterNot(packageInfo => {
      getPackageDirectory(projectLibDirectory, packageInfo).exists()
    }).flatMap { packageInfo =>
      val stderr = CommandLine.runProgram(Some(project), projectLibDirectory.getAbsolutePath, stackPath, Seq("unpack", packageInfo.directoryName), 10000, Some(CaptureOutputToLog)).map(_.getStderr)
      progressFraction = progressFraction + step
      progressIndicator.setFraction(progressFraction)

      if (stderr.exists(_.contains("not found"))) {
        Seq()
      } else {
        Seq(packageInfo)
      }
    }
    alreadyDownloadedPackages ++ downloadedPackages
  }

  private def getPackageDirectory(projectLibDirectory: File, packageInfo: HaskellPackageInfo) = {
    new File(projectLibDirectory, packageInfo.directoryName)
  }

  private def getUrlByPath(path: String): String = {
    VfsUtil.getUrlForLibraryRoot(new File(path))
  }

  private def addPackagesAsLibrariesToModule(module: Module, haskellPackages: Seq[HaskellPackageInfo], projectLibDirectory: File) {
    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      val libraryTable = modifiableRootModel.getModuleLibraryTable
      libraryTable.getLibraries.foreach(l => libraryTable.removeLibrary(l))

      haskellPackages.foreach(packageInfo => {
        val sourceRootUrl = getUrlByPath(getPackageDirectory(projectLibDirectory, packageInfo).getAbsolutePath)
        addProjectLibrary(module, modifiableRootModel, packageInfo.name, sourceRootUrl)
      })
    })
  }

  private def addProjectLibrary(module: Module, modifiableRootModel: ModifiableRootModel, ideaHaskellLibPath: String, sourceRootUrl: String): Unit = {
    val projectLibraryTable = ProjectLibraryTable.getInstance(module.getProject)
    new WriteAction[Library]() {

      override protected def run(result: Result[Library]): Unit = {
        val library = projectLibraryTable.createLibrary(ideaHaskellLibPath)
        val libraryModel = library.getModifiableModel
        libraryModel.addRoot(sourceRootUrl, OrderRootType.CLASSES)
        libraryModel.addRoot(sourceRootUrl, OrderRootType.SOURCES)
        libraryModel.commit()
        modifiableRootModel.addLibraryEntry(library)
      }
    }.execute
  }

  private case class HaskellPackageInfo(name: String, version: String, directoryName: String)

}
