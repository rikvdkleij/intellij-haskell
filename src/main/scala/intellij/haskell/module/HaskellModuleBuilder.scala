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

package intellij.haskell.module

import java.io.File

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.{ApplicationManager, WriteAction}
import com.intellij.openapi.module.{ModifiableModuleModel, Module, ModuleType}
import com.intellij.openapi.project.{Project, ProjectManager}
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots._
import com.intellij.openapi.roots.libraries.{Library, LibraryTablesRegistrar, LibraryUtil}
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.templates.TemplateModuleBuilder
import icons.HaskellIcons
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.component.{HaskellComponentsManager, PackageInfo}
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.{FutureUtil, HaskellFileUtil, HaskellProjectUtil, ScalaUtil}
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}
import javax.swing.Icon

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

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
      packageRelativePath.flatMap(pp => HaskellModuleBuilder.createCabalInfo(rootModel.getProject, project.getBasePath, pp)) match {
        case Some(ci) => cabalInfo = ci
        case None =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Couldn't create Haskell module due to failure retrieving or parsing Cabal file for package path `${project.getBasePath}`")
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
    val newProjectTemplateName = HaskellSettingsState.getNewProjectTemplateName

    if (isNewProjectWithoutExistingSources) {
      val createModuleAction = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
        val processOutput = StackCommandLine.run(project, Seq("new", project.getName, "--bare", newProjectTemplateName, "-p", "author-email:Author email here", "-p", "author-name:Author name here", "-p", "category:App category here", "-p", "copyright:2019 Author name here", "-p", "github-username:Github username here"), timeoutInMillis = 60.seconds.toMillis, enableExtraArguments = false)
        processOutput match {
          case None =>
            WriteAction.run {
              () => {
                Messages.showErrorDialog("Unknown error while creating new Stack project by using Stack command for creating new project on file system", "Create Haskell module")
              }
            }
          case Some(output) =>
            if (output.getExitCode != 0) {
              WriteAction.run {
                () => {
                  Messages.showErrorDialog(s"Error while creating new Stack project: ${output.getStdout} ${output.getStderr}", "Create Haskell module")
                }
              }
            }
        }
      })
      FutureUtil.waitForValue(project, createModuleAction, "Creating Haskell module", 120) match {
        case None => HaskellNotificationGroup.logErrorBalloonEvent(project, s"Timeout while creating new Stack project by using: `stack new`")
        case Some(_) => ()
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

  override def updateDataModel(): Unit = {
    super.updateDataModel()
    haskellModuleBuilder.setModuleJdk(getJdk)
  }

  override def validate(): Boolean = {
    if (getJdk == null) {
      Messages.showErrorDialog("You can't create a Haskell project without Stack configured as SDK", "No Haskell Tool Stack specified")
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
      cabalFile <- getCabalFile(project, moduleDirectory)
      cabalInfo <- getCabalInfo(project, cabalFile)
    } yield cabalInfo
  }

  def getStackWorkDirectory(moduleBuilder: ModuleBuilder): File = {
    new File(moduleBuilder.getContentEntryPath, GlobalInfo.StackWorkDirName)
  }

  private def getDependencies(project: Project, module: Module, packageName: String, libraryDependencies: Seq[HaskellLibraryDependency]): Seq[HaskellDependency] = {
    val cabalInfo = HaskellComponentsManager.findCabalInfos(project).find(_.packageName == packageName)
    val libPackages = cabalInfo.flatMap(_.library.map(_.buildDepends)).getOrElse(Seq())
    val exePackages = cabalInfo.map(_.executables.flatMap(_.buildDepends)).getOrElse(Seq())
    val testPackages = cabalInfo.map(_.testSuites.flatMap(_.buildDepends)).getOrElse(Seq())
    val benchPackages = cabalInfo.map(_.benchmarks.flatMap(_.buildDepends)).getOrElse(Seq())

    val packages = (libPackages ++ exePackages ++ testPackages ++ benchPackages).distinct.filterNot(n => n == packageName || n == "rts")

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
          cabalInfo.map(ci => HaskellModuleDependency(ci.packageName, ci.packageVersion, m))
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

  private def getCabalFile(project: Project, moduleDirectory: File): Option[File] = {
    HaskellProjectUtil.findCabalFile(moduleDirectory) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(s"Couldn't create Haskell module because Cabal file can't be found in `$moduleDirectory`", "Haskell module can't be created")
        None
    }
  }

  private def getCabalInfo(project: Project, cabalFile: File): Option[CabalInfo] = {
    CabalInfo.create(project, cabalFile) match {
      case Some(f) => Option(f)
      case None =>
        Messages.showErrorDialog(project, s"Couldn't create Haskell module because Cabal file `$cabalFile` can't be parsed", "Haskell module can't be created")
        None
    }
  }

  private def getDependsOnPackageInfos(libraryPackageInfos: Seq[PackageInfo], modulePackageInfos: Seq[PackageInfo]): Seq[PackageInfo] = {
    val libraryPackageInfoByName = libraryPackageInfos.map(pi => (pi.packageName, pi)).toMap

    @tailrec
    def go(packageInfos: Seq[PackageInfo], dependsOnPackageInfos: ListBuffer[PackageInfo]): Seq[PackageInfo] = {
      val dependsOn = packageInfos.flatMap { pi =>
        dependsOnPackageInfos += pi
        pi.dependsOnPackageIds.map(_.name).flatMap(libraryPackageInfoByName.get).filterNot(dependsOnPackageInfos.contains)
      }

      if (dependsOn.isEmpty) {
        dependsOnPackageInfos.toSeq
      } else {
        go(dependsOn.distinct, dependsOnPackageInfos)
      }
    }

    val dependsOnPackageInfos = ListBuffer[PackageInfo]()

    go(modulePackageInfos, dependsOnPackageInfos).filterNot(_.packageName == "rts")
  }

  private def getModuleLibraryDependencies(moduleDependencies: Seq[HaskellDependency], libraryPackageInfos: Seq[PackageInfo]): Seq[HaskellLibraryDependency] = {
    val moduleLibraryPackageInfos = moduleDependencies.filter(_.isInstanceOf[HaskellLibraryDependency]).flatMap(d => libraryPackageInfos.find(_.packageName == d.name))
    val dependsOnPackageInfos = getDependsOnPackageInfos(libraryPackageInfos, moduleLibraryPackageInfos)
    dependsOnPackageInfos.map(pi => HaskellLibraryDependency(pi.packageName, pi.version))
  }

  def addLibrarySources(project: Project, update: Boolean): Unit = {
    val projectLibDirectory = HaskellProjectUtil.getProjectLibrarySourcesDirectory(project)
    if (update || getProjectLibraryTable(project).getLibraries.isEmpty || !projectLibDirectory.exists()) {
      HaskellSdkType.getStackBinaryPath(project).foreach(stackPath => {

        StackCommandLine.updateStackIndex(project)

        if (!projectLibDirectory.exists()) {
          FileUtil.createDirectory(projectLibDirectory)
        }

        val libraryPackageInfos = HaskellComponentsManager.findLibraryPackageInfos(project)
        val libraryDependencies = libraryPackageInfos.map(pi => HaskellLibraryDependency(pi.packageName, pi.version))

        val projectModulePackageNames = HaskellComponentsManager.findProjectModulePackageNames(project)

        val dependenciesByModule = for {
          (module, packageName) <- projectModulePackageNames
          moduleDependencies = getDependencies(project, module, packageName, libraryDependencies)
        } yield (module, moduleDependencies, getModuleLibraryDependencies(moduleDependencies, libraryPackageInfos))

        val projectLibraryDependencies = dependenciesByModule.flatMap(_._3).distinct

        downloadHaskellPackageSources(project, projectLibDirectory, stackPath, projectLibraryDependencies)

        setupProjectLibraries(project, projectLibraryDependencies, projectLibDirectory)

        dependenciesByModule.foreach { case (module, moduleDependencies, moduleLibraryDependencies) =>
          setupModuleLibraries(module, moduleDependencies ++ moduleLibraryDependencies)
        }
      })
    }
  }

  private def downloadHaskellPackageSources(project: Project, projectLibDirectory: File, stackPath: String, libraryDependencies: Seq[HaskellLibraryDependency]): Unit = {
    libraryDependencies.filterNot(libraryDependency => getPackageDirectory(projectLibDirectory, libraryDependency).exists()).foreach(libraryDependency => {
      CommandLine.run1(project, projectLibDirectory.getAbsolutePath, stackPath, Seq("--no-nix", "unpack", libraryDependency.nameVersion), 10000)
    })
  }

  private def getPackageDirectory(projectLibDirectory: File, libraryDependency: HaskellLibraryDependency) = {
    new File(projectLibDirectory, libraryDependency.nameVersion)
  }

  private def getProjectLibraryTable(project: Project) = {
    LibraryTablesRegistrar.getInstance.getLibraryTable(project)
  }

  private def setupProjectLibraries(project: Project, libraryDependencies: Seq[HaskellLibraryDependency], projectLibDirectory: File): Unit = {
    getProjectLibraryTable(project).getLibraries.foreach(library => {
      libraryDependencies.find(_.nameVersion == library.getName) match {
        case Some(_) =>
          if (library.getFiles(OrderRootType.SOURCES).isEmpty) {
            removeProjectLibrary(project, library)
          }
        case None => removeProjectLibrary(project, library)
      }
    })

    libraryDependencies.foreach(dependency => {
      val projectLibrary = getProjectLibraryTable(project).getLibraryByName(dependency.nameVersion)
      if (projectLibrary == null) {
        createProjectLibrary(project, dependency, projectLibDirectory)
      }
    })
  }

  private def findLibraryDependency(dependencies: Seq[HaskellDependency], name: String) = {
    dependencies.find {
      case d: HaskellLibraryDependency => d.nameVersion == name
      case _ => false
    }
  }

  private def findModuleDependency(dependencies: Seq[HaskellDependency], module: Module) = {
    dependencies.find {
      case d: HaskellModuleDependency => d.module == module
      case _ => false
    }
  }

  private def setupModuleLibraries(module: Module, moduleDependencies: Seq[HaskellDependency]): Unit = {
    val project = module.getProject

    ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
      modifiableRootModel.getOrderEntries.foreach {
        case e: LibraryOrderEntry => if (findLibraryDependency(moduleDependencies, e.getLibraryName).isEmpty) modifiableRootModel.removeOrderEntry(e)
        case e: ModuleOrderEntry => if (findModuleDependency(moduleDependencies, e.getModule).isEmpty) modifiableRootModel.removeOrderEntry(e)
        case _ => ()
      }
    })

    moduleDependencies.foreach {
      case d: HaskellLibraryDependency =>
        if (LibraryUtil.findLibrary(module, d.nameVersion) == null) {
          val projectLibrary = getProjectLibraryTable(project).getLibraryByName(d.nameVersion)
          if (projectLibrary == null) {
            HaskellNotificationGroup.logInfoEvent(project, "Could not find project library " + projectLibrary.getName)
          }
          addModuleLibrary(module, projectLibrary)
        }
      case d: HaskellModuleDependency =>
        ModuleRootModificationUtil.updateModel(module, (modifiableRootModel: ModifiableRootModel) => {
          if (module != d.module && modifiableRootModel.findModuleOrderEntry(d.module) == null) {
            modifiableRootModel.addModuleOrderEntry(d.module)
          }
        })
    }
  }

  private def removeProjectLibrary(project: Project, library: Library): Unit = {
    getProjectLibraryTable(project).getLibraries.find(_.getName == library.getName).foreach(library => {
      val model = getProjectLibraryTable(project).getModifiableModel
      model.removeLibrary(library)
      ApplicationManager.getApplication.invokeAndWait(ScalaUtil.runnable(WriteAction.run(() => model.commit())))
    })
  }

  private def createProjectLibrary(project: Project, libraryDependency: HaskellLibraryDependency, projectLibDirectory: File): Library = {
    val projectLibraryTableModel = getProjectLibraryTable(project).getModifiableModel
    val (libraryName, sourceRootPath) = (libraryDependency.nameVersion, getPackageDirectory(projectLibDirectory, libraryDependency))
    val library = projectLibraryTableModel.createLibrary(libraryName)
    val libraryModel = library.getModifiableModel
    val sourceRootUrl = HaskellFileUtil.getUrlByPath(sourceRootPath.getAbsolutePath)
    libraryModel.addRoot(sourceRootUrl, OrderRootType.CLASSES)
    libraryModel.addRoot(sourceRootUrl, OrderRootType.SOURCES)

    ApplicationManager.getApplication.invokeAndWait(ScalaUtil.runnable(WriteAction.run(() => libraryModel.commit())))
    ApplicationManager.getApplication.invokeAndWait(ScalaUtil.runnable(WriteAction.run(() => projectLibraryTableModel.commit())))
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

    def nameVersion = s"$name-$version"
  }

  case class HaskellLibraryDependency(name: String, version: String) extends HaskellDependency

  case class HaskellModuleDependency(name: String, version: String, module: Module) extends HaskellDependency

}
