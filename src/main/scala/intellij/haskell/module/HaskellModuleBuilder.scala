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
import com.intellij.openapi.application.{ApplicationManager, Result, RunResult, WriteAction}
import com.intellij.openapi.module.{Module, ModuleType}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots._
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable
import com.intellij.openapi.roots.libraries.{Library, LibraryUtil}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VfsUtil, VirtualFileManager}
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.execution.{CaptureOutputToLog, CommandLine, StackCommandLine}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}
import intellij.haskell.{HaskellIcons, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.collection.mutable
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
      val packageRelativePath = StackYamlComponent.getPackagePaths(project).flatMap(_.headOption)
      packageRelativePath.flatMap(pp => HaskellModuleBuilder.createCabalInfo(rootModel.getProject, HaskellFileUtil.getAbsoluteFilePath(project.getBaseDir), pp)) match {
        case Some(ci) => cabalInfo = ci
        case None =>
          Messages.showErrorDialog(s"Could not create Haskell module because could not retrieve or parse Cabal file for package path $packageRelativePath", "No Cabal file info")
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
  private final val PackagePattern = """([\w\-]+)\s([\d\.]+)""".r

  def createCabalInfo(project: Project, modulePath: String, packageRelativePath: String): Option[CabalInfo] = {
    for {
      moduleDirectory <- getModuleRootDirectory(packageRelativePath, modulePath)
      cabalFile <- getCabalFile(moduleDirectory)
      cabalInfo <- getCabalInfo(project, cabalFile)
    } yield cabalInfo
  }

  private def getModuleRootDirectory(packagePath: String, modulePath: String): Option[File] = {
    val file = if (packagePath == ".") {
      new File(modulePath)
    } else {
      new File(modulePath, packagePath)
    }
    Option(file).filter(_.exists())
  }

  private def getCabalFile(moduleDirectory: File): Option[File] = {
    HaskellProjectUtil.findCabalFile(moduleDirectory) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(s"Could not create Haskell module because Cabal file can not be found in $moduleDirectory", "Haskell module can not be created")
        None
    }
  }

  private def getCabalInfo(project: Project, cabalFile: File): Option[CabalInfo] = {
    CabalInfo.create(project, cabalFile) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(project, s"Could not create Haskell module because Cabal file $cabalFile can not be parsed", "Haskell module can not be created")
        None
    }
  }

  def addLibrarySources(project: Project): Unit = {
    HaskellSdkType.getStackPath(project).foreach(stackPath => {

      removeOldIdeaHaskellLibDirectory(project)

      val projectLibDirectory = getProjectLibDirectory(project)
      if (!projectLibDirectory.exists()) {
        FileUtil.createDirectory(projectLibDirectory)
      }

      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading Haskell library sources and adding them as source libraries to module") {

        def run(progressIndicator: ProgressIndicator) {
          val projectModules = HaskellProjectUtil.findProjectModules(project)
          val packageInfosByModule = for {
            module <- projectModules
            packageName = module.getName
            lines <- StackCommandLine.runCommand(project, Seq("list-dependencies", packageName, "--test", "--bench"), timeoutInMillis = 60.seconds.toMillis).map(_.getStdoutLines)
            packageInfos = createPackageInfos(project, lines.asScala).filterNot(p => packageName == p.name || p.name == "rts" || p.name == "ghc")
          } yield (module, packageInfos)

          val projectPackageNames = projectModules.map(_.getName)
          val libraryPackageInfos = packageInfosByModule.foldLeft[mutable.ListBuffer[HaskellPackageInfo]](mutable.ListBuffer()) { case (xs, (_, y)) => xs.++=(y) }.distinct.filterNot(info => projectPackageNames.exists(_ == info.name))

          downloadHaskellPackageSources(project, projectLibDirectory, stackPath, libraryPackageInfos)

          packageInfosByModule.foreach { case (module, modulePackageinfos) =>
            addPackagesAsLibrariesToModule(module, projectModules, modulePackageinfos, libraryPackageInfos, projectLibDirectory)
          }
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
    val homeDirectory = HaskellFileUtil.getAbsoluteFilePath(VfsUtil.getUserHomeDir)
    new File(new File(homeDirectory, IdeaHaskellLibName), project.getName)
  }

  private def createPackageInfos(project: Project, dependencyLines: Seq[String]): Seq[HaskellPackageInfo] = {
    val packageInfos = dependencyLines.flatMap {
      case PackagePattern(name, version) => Option(HaskellPackageInfo(name, version, s"$name-$version"))
      case x =>
        HaskellNotificationGroup.logWarningEvent(project, s"Could not determine package for line `$x`")
        None
    }
    packageInfos
  }

  private def downloadHaskellPackageSources(project: Project, projectLibDirectory: File, stackPath: String, packageInfos: Seq[HaskellPackageInfo]): Unit = {
    packageInfos.filterNot(packageInfo => getPackageDirectory(projectLibDirectory, packageInfo).exists()).flatMap(packageInfo => {
      val stderr = CommandLine.runProgram(Some(project), projectLibDirectory.getAbsolutePath, stackPath, Seq("unpack", packageInfo.nameVersion), 10000, Some(CaptureOutputToLog)).map(_.getStderr)
      if (stderr.exists(_.contains("not found"))) {
        Seq()
      } else {
        Seq(packageInfo)
      }
    })

    ApplicationManager.getApplication.invokeAndWait(() => {
      VirtualFileManager.getInstance().syncRefresh()
    })
  }

  private def getPackageDirectory(projectLibDirectory: File, packageInfo: HaskellPackageInfo) = {
    new File(projectLibDirectory, packageInfo.nameVersion)
  }


  private def getProjectLibrary(project: Project) = {
    ProjectLibraryTable.getInstance(project)
  }

  private def addPackagesAsLibrariesToModule(module: Module, projectModules: Iterable[Module], modulePackageInfos: Seq[HaskellPackageInfo], projectPackageInfos: Seq[HaskellPackageInfo], projectLibDirectory: File) {
    val project = module.getProject
    getProjectLibrary(project).getLibraries.foreach(library => {
      val packageInfo = modulePackageInfos.find(_.nameVersion == library.getName)
      packageInfo match {
        case Some(_) =>
          if (LibraryUtil.findLibrary(module, library.getName) == null) {
            addModuleLibrary(module, library)
          }
        case None =>
          removeModuleLibrary(module, library)
          if (!projectPackageInfos.exists(_.nameVersion == library.getName)) {
            removeProjectLibrary(module.getProject, library)
            FileUtil.delete(new File(projectLibDirectory, library.getName))
          }
      }
    })

    modulePackageInfos.foreach(packageInfo => {
      val projectLibrary = getProjectLibrary(project).getLibraryByName(packageInfo.nameVersion)
      if (projectLibrary == null) {
        val projectLibrary = createProjectLibrary(module.getProject, projectModules, packageInfo, projectLibDirectory)
        addModuleLibrary(module, projectLibrary)
      } else {
        if (LibraryUtil.findLibrary(module, projectLibrary.getName) == null) {
          addModuleLibrary(module, projectLibrary)
        }
      }
    })
  }

  private def removeModuleLibrary(module: Module, library: Library): Unit = {
    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      val moduleLibrary = LibraryUtil.findLibrary(module, library.getName)
      if (moduleLibrary != null) {
        val libraryOrderEntry = modifiableRootModel.findLibraryOrderEntry(moduleLibrary)
        modifiableRootModel.removeOrderEntry(libraryOrderEntry)
      }
    })
  }

  private def removeProjectLibrary(project: Project, library: Library): RunResult[Unit] = {
    new WriteAction[Unit]() {

      def run(result: Result[Unit]): Unit = {
        getProjectLibrary(project).getLibraries.find(_.getName == library.getName).foreach(library => {
          val model = getProjectLibrary(project).getModifiableModel
          model.removeLibrary(library)
          model.commit()
        })
      }
    }.execute()
  }

  private def createProjectLibrary(project: Project, projectModules: Iterable[Module], packageInfo: HaskellPackageInfo, projectLibDirectory: File): Library = {
    val projectLibraryTable = getProjectLibrary(project)
    val library = new WriteAction[Library]() {

      def run(result: Result[Library]): Unit = {
        val projectModule = projectModules.find(_.getName == packageInfo.name)
        val (libraryName, sourceRootPath) = projectModule match {
          case Some(m) => (packageInfo.name, HaskellProjectUtil.getModulePath(m).getAbsolutePath)
          case None => (packageInfo.nameVersion, getPackageDirectory(projectLibDirectory, packageInfo).getAbsolutePath)
        }
        val library = projectLibraryTable.createLibrary(libraryName)
        val libraryModel = library.getModifiableModel
        val sourceRootUrl = HaskellFileUtil.getUrlByPath(sourceRootPath)
        libraryModel.addRoot(sourceRootUrl, OrderRootType.CLASSES)
        libraryModel.addRoot(sourceRootUrl, OrderRootType.SOURCES)
        libraryModel.commit()
        result.setResult(library)
      }
    }.execute
    library.getResultObject
  }

  private def addModuleLibrary(module: Module, library: Library): Unit = {
    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      modifiableRootModel.addLibraryEntry(library)
    })
  }

  private case class HaskellPackageInfo(name: String, version: String, nameVersion: String)

}
