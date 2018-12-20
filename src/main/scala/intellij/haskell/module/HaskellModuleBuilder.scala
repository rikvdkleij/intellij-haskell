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

package intellij.haskell.module

import java.io.File

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.{ApplicationManager, WriteAction}
import com.intellij.openapi.module.{ModifiableModuleModel, Module, ModuleType}
import com.intellij.openapi.project.{DumbService, Project, ProjectManager, ProjectUtil}
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots._
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable
import com.intellij.openapi.roots.libraries.{Library, LibraryUtil}
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.templates.TemplateModuleBuilder
import icons.HaskellIcons
import intellij.haskell.GlobalInfo
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaUtil}
import javax.swing.Icon

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class HaskellModuleBuilder extends TemplateModuleBuilder(null, HaskellModuleType.getInstance, List().asJava) {

  private var cabalInfo: CabalInfo = _

  def setCabalInfo(cabalInfo: CabalInfo): Unit = {
    this.cabalInfo = cabalInfo
  }

  private[this] var isNewProjectWithoutExistingSources = false

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = HaskellModuleType.getInstance

  override def isSuitableSdkType(sdkType: SdkTypeId): Boolean = {
    sdkType == HaskellSdkType.getInstance
  }

  override def getNodeIcon: Icon = HaskellIcons.HaskellLogo

  override def setupRootModel(rootModel: ModifiableRootModel): Unit = {
    if (rootModel.getSdk == null) {
      rootModel.setSdk(HaskellSdkType.findOrCreateSdk())
      rootModel.inheritSdk()
    }

    val contentEntry = doAddContentEntry(rootModel)
    val project = rootModel.getProject

    if (isNewProjectWithoutExistingSources) {
      val packageRelativePath = StackYamlComponent.getPackagePaths(project).flatMap(_.headOption)
      packageRelativePath.flatMap(pp => HaskellModuleBuilder.createCabalInfo(rootModel.getProject, HaskellFileUtil.getAbsolutePath(ProjectUtil.guessProjectDir(project)), pp)) match {
        case Some(ci) => cabalInfo = ci
        case None =>
          Messages.showErrorDialog(s"Could not create Haskell module because could not retrieve or parse Cabal file for package path `$packageRelativePath`", "No Cabal file info")
      }
    }

    if (contentEntry != null) {
      HaskellModuleBuilder.addSourceFolders(cabalInfo, contentEntry)

      val stackWorkDirectory = HaskellModuleBuilder.getStackWorkDirectory(this)
      stackWorkDirectory.mkdir()
      Option(LocalFileSystem.getInstance.refreshAndFindFileByIoFile(stackWorkDirectory)).foreach(f =>
        contentEntry.addExcludeFolder(f)
      )
    }
  }

  // Only called in case new project without existing Stack project
  override def getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep = {
    isNewProjectWithoutExistingSources = true
    new HaskellModuleWizardStep(context, this)
  }

  override def createModule(moduleModel: ModifiableModuleModel): Module = {
    ModuleBuilder.deleteModuleFile(getModuleFilePath)
    val moduleType = getModuleType
    val module = moduleModel.newModule(getModuleFilePath, moduleType.getId)
    val project = module.getProject

    if (isNewProjectWithoutExistingSources) {
      val processOutput = StackCommandLine.run(project, Seq("new", project.getName, "--bare", "new-template", "-p", "author-email:Author email here", "-p", "author-name:Author name here", "-p", "category:App category here", "-p", "copyright:2018 Author name here", "-p", "github-username:Github username here"), timeoutInMillis = 60.seconds.toMillis)
      processOutput match {
        case None =>
          throw new RuntimeException("Could not create new Stack project because could not execute Stack command for creating new project on file system")
        case Some(output) =>
          if (output.getExitCode != 0) {
            throw new RuntimeException(s"Could not create new Stack project: ${output.getStdout} ${output.getStderr}")
          }
      }
    }
    setupModule(module)
    module
  }

  override def getBuilderId: String = {
    val moduleType = getModuleType()
    if (moduleType == null) null else moduleType.getId
  }

  override def createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array[ModuleWizardStep] = {
    Array()
  }

  override def createFinishingSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array[ModuleWizardStep] = {
    Array()
  }

  override def createProject(name: String, path: String): Project = ProjectManager.getInstance.createProject(name, path)

  // To prevent first page of wizard is empty.
  override def isTemplateBased: Boolean = false

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

  def addSourceFolders(cabalInfo: CabalInfo, contentEntry: ContentEntry): Unit = {
    cabalInfo.sourceRoots.foreach(path => {
      Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))).foreach(f =>
        contentEntry.addSourceFolder(f, false)
      )
    })

    cabalInfo.testSourceRoots.foreach(path => {
      Option(LocalFileSystem.getInstance.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))).foreach(f =>
        contentEntry.addSourceFolder(f, true)
      )
    })
  }

  def createCabalInfo(project: Project, modulePath: String, packageRelativePath: String): Option[CabalInfo] = {
    val moduleDirectory = getModuleRootDirectory(packageRelativePath, modulePath)
    for {
      cabalFile <- getCabalFile(moduleDirectory)
      cabalInfo <- getCabalInfo(project, cabalFile)
    } yield cabalInfo
  }

  def getStackWorkDirectory(moduleBuilder: ModuleBuilder): File = {
    new File(moduleBuilder.getContentEntryPath, GlobalInfo.StackWorkDirName)
  }

  private def getDependencies(project: Project, module: Module, packageName: String, libraryDependencies: Seq[HaskellLibraryDependency]): Seq[HaskellDependency] = {
    val cabalInfo = HaskellComponentsManager.findCabalInfos(project).find(_.packageName == packageName)
    val libPackages = cabalInfo.flatMap(_.library.map(_.buildDepends)).getOrElse(Array())
    val exePackages = cabalInfo.map(_.executables.flatMap(_.buildDepends)).getOrElse(Seq())
    val testPackages = cabalInfo.map(_.testSuites.flatMap(_.buildDepends)).getOrElse(Seq())
    val benchPackages = cabalInfo.map(_.benchmarks.flatMap(_.buildDepends)).getOrElse(Seq())

    val packages = (libPackages ++ exePackages ++ testPackages ++ benchPackages).distinct.filterNot(n => n == packageName || n == "rts" || n == "ghc")

    val projectModulePackageNames = HaskellComponentsManager.findProjectModulePackageNames(project)

    packages.flatMap(n => {
      projectModulePackageNames.find(_._2 == n).map(_._1) match {
        case None =>
          libraryDependencies.find(_.name == n) match {
            case Some(info) => Some(HaskellLibraryDependency(info.name, info.version))
            case None => None
          }
        case Some(m) =>
          val cabalInfo = HaskellComponentsManager.findCabalInfos(project).find(_.packageName == n)
          cabalInfo.map(ci => HaskellProjectModuleDependency(ci.packageName, ci.packageVersion, m))
      }
    })
  }

  def getModuleRootDirectory(packagePath: String, modulePath: String): File = {
    if (packagePath == ".") {
      new File(modulePath).getCanonicalFile
    } else {
      new File(modulePath, packagePath).getCanonicalFile
    }
  }

  private def getCabalFile(moduleDirectory: File): Option[File] = {
    HaskellProjectUtil.findCabalFile(moduleDirectory) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(s"Could not create Haskell module because Cabal file can not be found in `$moduleDirectory`", "Haskell module can not be created")
        None
    }
  }

  private def getCabalInfo(project: Project, cabalFile: File): Option[CabalInfo] = {
    CabalInfo.create(project, cabalFile) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(project, s"Could not create Haskell module because Cabal file `$cabalFile` can not be parsed", "Haskell module can not be created")
        None
    }
  }

  def addLibrarySources(project: Project, update: Boolean): Unit = {
    val projectLibDirectory = getProjectLibDirectory(project)
    if (update || getProjectLibraryTable(project).getLibraries.isEmpty || !projectLibDirectory.exists()) {
      HaskellSdkType.getStackBinaryPath(project).foreach(stackPath => {

        if (!projectLibDirectory.exists()) {
          FileUtil.createDirectory(projectLibDirectory)
        }

        val libraryDependencies = HaskellComponentsManager.findLibraryPackageInfos(project).map(info => HaskellLibraryDependency(info.packageName, info.version))

        val projectModulePackageNames = HaskellComponentsManager.findProjectModulePackageNames(project)
        val dependenciesByModule = for {
          (module, packageName) <- projectModulePackageNames
          moduleDependencies = getDependencies(project, module, packageName, libraryDependencies)
        } yield (module, moduleDependencies)

        downloadHaskellPackageSources(project, projectLibDirectory, stackPath, libraryDependencies)

        dependenciesByModule.foreach { case (module, moduledependencies) =>
          addPackagesAsDependenciesToModule(module, projectModulePackageNames.map(_._1), moduledependencies, libraryDependencies, projectLibDirectory)
        }
      })
    }
  }

  private def getProjectLibDirectory(project: Project): File = {
    new File(GlobalInfo.getLibrarySourcesPath, project.getName)
  }

  private def downloadHaskellPackageSources(project: Project, projectLibDirectory: File, stackPath: String, libraryDependencies: Seq[HaskellLibraryDependency]): Unit = {
    libraryDependencies.filterNot(libraryDependency => getPackageDirectory(projectLibDirectory, libraryDependency).exists()).flatMap(libraryDependency => {
      CommandLine.run1(project, projectLibDirectory.getAbsolutePath, stackPath, Seq("--no-nix", "unpack", libraryDependency.nameVersion), 10000).getStderr
    })
  }

  private def getPackageDirectory(projectLibDirectory: File, libraryDependency: HaskellLibraryDependency) = {
    new File(projectLibDirectory, libraryDependency.nameVersion)
  }

  private def getProjectLibraryTable(project: Project) = {
    ProjectLibraryTable.getInstance(project)
  }

  private def addPackagesAsDependenciesToModule(module: Module, projectModules: Iterable[Module], moduleDependencies: Iterable[HaskellDependency], libraryDependencies: Seq[HaskellLibraryDependency], projectLibDirectory: File): Unit = {
    val project = module.getProject
    getProjectLibraryTable(project).getLibraries.foreach(library => {
      DumbService.getInstance(project).waitForSmartMode()
      moduleDependencies.find(_.nameVersion == library.getName) match {
        case Some(_) =>
          if (LibraryUtil.findLibrary(module, library.getName) == null) {
            addModuleLibrary(module, library)
          }
        case None =>
          removeModuleLibrary(module, library)
          if (!libraryDependencies.exists(_.nameVersion == library.getName)) {
            removeProjectLibrary(module.getProject, library)
            FileUtil.delete(new File(projectLibDirectory, library.getName))
          }
      }
    })

    val projectModuleDependencies = moduleDependencies.filter(_.isInstanceOf[HaskellProjectModuleDependency]).map(_.asInstanceOf[HaskellProjectModuleDependency])

    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      DumbService.getInstance(project).waitForSmartMode()
      modifiableRootModel.getModuleDependencies.foreach(m => {
        if (!projectModuleDependencies.exists(_.projectModule == m)) {
          Option(modifiableRootModel.findModuleOrderEntry(m)).foreach(modifiableRootModel.removeOrderEntry)
        }
      })
    })

    libraryDependencies.foreach(dependency => {
      DumbService.getInstance(project).waitForSmartMode()
      val projectLibrary = getProjectLibraryTable(project).getLibraryByName(dependency.nameVersion)
      if (projectLibrary == null) {
        val projectLibrary = createProjectLibrary(module.getProject, dependency, projectLibDirectory)
        addModuleLibrary(module, projectLibrary)
      } else {
        if (LibraryUtil.findLibrary(module, projectLibrary.getName) == null) {
          addModuleLibrary(module, projectLibrary)
        }
      }
    })

    projectModuleDependencies.foreach(dependency => {
      DumbService.getInstance(project).waitForSmartMode()
      ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
        if (module != dependency.projectModule && modifiableRootModel.findModuleOrderEntry(dependency.projectModule) == null) {
          modifiableRootModel.addModuleOrderEntry(dependency.projectModule)
        }
      })
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

  private def removeProjectLibrary(project: Project, library: Library): Unit = {
    getProjectLibraryTable(project).getLibraries.find(_.getName == library.getName).foreach(library => {
      val model = getProjectLibraryTable(project).getModifiableModel
      model.removeLibrary(library)
      ApplicationManager.getApplication.invokeLater(ScalaUtil.runnable(WriteAction.run(() => model.commit())))
    })
  }

  private def createProjectLibrary(project: Project, libraryDependency: HaskellLibraryDependency, projectLibDirectory: File): Library = {
    val projectLibraryTableModel = getProjectLibraryTable(project).getModifiableModel
    val (libraryName, sourceRootPath) = (libraryDependency.nameVersion, getPackageDirectory(projectLibDirectory, libraryDependency).getAbsolutePath)
    val library = projectLibraryTableModel.createLibrary(libraryName)
    val libraryModel = library.getModifiableModel
    val sourceRootUrl = HaskellFileUtil.getUrlByPath(sourceRootPath)
    libraryModel.addRoot(sourceRootUrl, OrderRootType.CLASSES)
    libraryModel.addRoot(sourceRootUrl, OrderRootType.SOURCES)

    ApplicationManager.getApplication.invokeLater(ScalaUtil.runnable(WriteAction.run(() => libraryModel.commit())))
    ApplicationManager.getApplication.invokeLater(ScalaUtil.runnable(WriteAction.run(() => projectLibraryTableModel.commit())))
    library
  }

  private def addModuleLibrary(module: Module, library: Library): Unit = {
    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      modifiableRootModel.addLibraryEntry(library)
    })
  }

  trait HaskellDependency {
    def name: String

    def version: String

    def nameVersion: String = s"$name-$version"
  }

  case class HaskellLibraryDependency(name: String, version: String) extends HaskellDependency

  case class HaskellProjectModuleDependency(name: String, version: String, projectModule: Module) extends HaskellDependency


}
