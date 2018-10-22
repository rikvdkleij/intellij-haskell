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

import java.util.concurrent.Executors

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.component.DefinitionLocationComponent.DefinitionLocationResult
import intellij.haskell.external.component.NameInfoComponentResult.NameInfoResult
import intellij.haskell.external.component.TypeInfoComponentResult.TypeInfoResult
import intellij.haskell.external.execution.CompilationResult
import intellij.haskell.external.repl.StackRepl.StanzaType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.module.HaskellModuleBuilder.HaskellDependency
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.{HaskellFileIndex, HaskellModuleNameIndex}
import intellij.haskell.util.{GhcVersion, HaskellProjectUtil, ScalaUtil}

import scala.concurrent._
import scala.concurrent.duration._

object HaskellComponentsManager {

  case class StackComponentInfo(module: Module, packageName: String, target: String, stanzaType: StanzaType, sourceDirs: Seq[String], mainIs: Option[String], isImplicitPreludeActive: Boolean, buildDepends: Seq[String], exposedModuleNames: Seq[String] = Seq.empty)

  def findPreloadedModuleIdentifiers(project: Project)(implicit ec: ExecutionContext): Iterable[ModuleIdentifier] = {
    val moduleNames = BrowseModuleComponent.findModuleNamesInCache(project)
    moduleNames.flatMap(mn => {
      try {
        val f = findLibraryModuleIdentifiers(project, mn)
        new WaitFor(1000, 1) {
          override def condition(): Boolean = {
            ProgressManager.checkCanceled()
            f.isCompleted
          }
        }

        if (f.isCompleted) {
          Await.result(f, 1.milli)
        } else {
          None
        }

      } catch {
        case _: TimeoutException => Iterable()
      }
    })
  }

  def clearLoadedModule(psiFile: PsiFile): Unit = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.foreach(_.clearLoadedModule())
  }

  def isReplBusy(project: Project): Boolean = {
    StackReplsManager.getRunningProjectRepls(project).exists(_.isBusy)
  }

  def isReplBusy(psiFile: PsiFile): Boolean = {
    LoadComponent.isBusy(psiFile)
  }

  def findLibraryModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    BrowseModuleComponent.findLibraryModuleIdentifiers(project, moduleName)
  }

  def findExportedModuleIdentifiers(stackComponentGlobalInfo: StackComponentGlobalInfo, psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    BrowseModuleComponent.findExportedIdentifiers(stackComponentGlobalInfo, psiFile, moduleName)
  }

  def findTopLevelModuleIdentifiers(psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    BrowseModuleComponent.findTopLevelIdentifiers(psiFile, moduleName)
  }

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement): DefinitionLocationResult = {
    DefinitionLocationComponent.findDefinitionLocation(psiFile, qualifiedNameElement)
  }

  def findNameInfo(qualifiedNameElement: HaskellQualifiedNameElement): Option[NameInfoResult] = {
    NameInfoComponent.findNameInfo(qualifiedNameElement)
  }

  def findNameInfo(psiElement: PsiElement): Option[NameInfoResult] = {
    NameInfoComponent.findNameInfo(psiElement)
  }

  def findNameInfoByModuleName(project: Project, moduleName: String, name: String): NameInfoResult = {
    NameInfoComponent.NameInfoByModuleComponent.findNameInfoByModuleName(project, moduleName, name)
  }

  def findAvailableModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableModuleNamesWithIndex(stackComponentInfo)
  }

  def findAvailableModuleLibraryModuleNamesWithIndex(module: Module): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableModuleLibraryModuleNamesWithIndex(module)
  }

  def findStackComponentGlobalInfo(stackComponentInfo: StackComponentInfo): Option[StackComponentGlobalInfo] = {
    StackComponentGlobalInfoComponent.findStackComponentGlobalInfo(stackComponentInfo)
  }

  def findStackComponentInfo(psiFile: PsiFile): Option[StackComponentInfo] = {
    HaskellProjectFileInfoComponent.findHaskellProjectFileInfo(psiFile).map(_.stackComponentInfo)
  }

  def findStackComponentInfo(project: Project, filePath: String): Option[StackComponentInfo] = {
    HaskellProjectFileInfoComponent.findHaskellProjectFileInfo(project, filePath).map(_.stackComponentInfo)
  }

  def getSupportedLanguageExtension(project: Project): Iterable[String] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.supportedLanguageExtensions).getOrElse(Iterable())
  }

  def getGhcVersion(project: Project): Option[GhcVersion] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.ghcVersion)
  }

  def getAvailableStackagePackages(project: Project): Iterable[String] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.availableStackagePackageNames).getOrElse(Iterable())
  }

  def getDependencies(project: Project): Iterable[HaskellDependency] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.dependencies).getOrElse(Iterable())
  }

  def findProjectPackageNames(project: Project): Option[Iterable[String]] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (_, ci) => ci.packageName })
  }

  def findCabalInfos(project: Project): Iterable[CabalInfo] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (m, ci) => ci }).getOrElse(Iterable())
  }

  def loadHaskellFile(psiFile: PsiFile, fileChanged: Boolean, psiElement: Option[PsiElement]): Option[CompilationResult] = {
    LoadComponent.load(psiFile, fileChanged, psiElement)
  }

  def invalidateHaskellFileInfoCache(psiFile: PsiFile): Unit = {
    HaskellProjectFileInfoComponent.invalidate(psiFile)
  }

  def invalidateDefinitionLocationCache(elements: Seq[HaskellQualifiedNameElement]): Unit = {
    DefinitionLocationComponent.invalidate(elements)
  }

  def findProjectModulePackageNames(project: Project): Iterable[(Module, String)] = {
    StackReplsManager.getReplsManager(project).map(_.stackComponentInfos).getOrElse(Seq()).map(info => (info.module, info.packageName))
  }

  def findReferencesInCache(targetFile: PsiFile): Seq[(PsiFile, HaskellQualifiedNameElement)] = {
    DefinitionLocationComponent.findReferencesInCache(targetFile)
  }

  def invalidateCachesForModules(project: Project, moduleNames: Seq[String]): Unit = {
    //    DefinitionLocationComponent.invalidate(psiFile)
    //    TypeInfoComponent.invalidate(psiFile)
    moduleNames.foreach(mn => BrowseModuleComponent.invalidateForModuleName(project, mn))
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to invalidate cache")
    GlobalProjectInfoComponent.invalidate(project)
    LibraryModuleNamesComponent.invalidate(project)
    HaskellProjectFileInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    DefinitionLocationComponent.invalidateAll(project)
    TypeInfoComponent.invalidateAll(project)
    HaskellPsiUtil.invalidateAllModuleNames(project)
    LibraryModuleNamesComponent.invalidate(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with invalidating cache")
  }

  def preloadLibraryIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library identifiers cache")
    preloadLibraryIdentifiers(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library identifiers cache")
  }

  def preloadLibraryModuleNamesCache(project: Project): Iterable[LibraryModuleNames] = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library modules cache")
    val result = preloadLibraryModuleNames(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library modules cache")
    result
  }

  def preloadLibraryFilesCache(project: Project, libraryModuleNames: Iterable[LibraryModuleNames]): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library files cache")
    preloadAllLibraryFiles(project, libraryModuleNames)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library files cache")
  }

  def findTypeInfoForElement(psiElement: PsiElement): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def preloadLibraryModuleNames(project: Project): Seq[LibraryModuleNames] = {
    if (!project.isDisposed) {
      val projectLibraryModuleNames = getDependencies(project).flatMap(d => LibraryModuleNamesComponent.findLibraryModuleNames(project, d.name))

      StackReplsManager.getReplsManager(project).foreach(_.stackComponentInfos.foreach(findStackComponentGlobalInfo))

      projectLibraryModuleNames.toSeq
    } else {
      Seq()
    }
  }

  private def preloadAllLibraryFiles(project: Project, libraryModuleNames: Iterable[LibraryModuleNames]): Unit = {
    if (!project.isDisposed) {
      DumbService.getInstance(project).waitForSmartMode()
      if (!project.isDisposed) {
        HaskellModuleNameIndex.fillCache(project, libraryModuleNames.flatMap(libraryModuleNames => libraryModuleNames.exposed ++ libraryModuleNames.hidden))
      }
    }
  }

  private val ExecutorService = Executors.newCachedThreadPool()
  implicit val ExecContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(ExecutorService)

  private def preloadLibraryIdentifiers(project: Project): Unit = {

    if (!project.isDisposed) {
      DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(BrowseModuleComponent.findLibraryModuleIdentifiers(project, HaskellProjectUtil.Prelude)))
    }

    if (!project.isDisposed) {
      val projectHaskellFiles =
        DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(
          if (project.isDisposed) {
            Iterable()
          } else {
            HaskellFileIndex.findProjectProductionHaskellFiles(project)
          }
        ))

      val componentInfos = projectHaskellFiles.flatMap(f => HaskellComponentsManager.findStackComponentInfo(f)).toSeq.distinct

      val importedLibraryModuleNames =
        projectHaskellFiles.flatMap(f => {
          if (project.isDisposed) {
            Iterable()
          } else {
            val libraryModuleNames = componentInfos.flatMap(HaskellComponentsManager.findStackComponentGlobalInfo).flatMap(_.libraryModuleNames)

            val exposedlibraryModuleNames = libraryModuleNames.flatMap(_.exposed).distinct
            DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable {
              HaskellPsiUtil.findImportDeclarations(f).flatMap(_.getModuleName).filter(mn => exposedlibraryModuleNames.contains(mn)).filterNot(_ == HaskellProjectUtil.Prelude)
            })
          }
        })

      if (!project.isDisposed) {
        importedLibraryModuleNames.toSeq.distinct.foreach(mn => {
          if (!project.isDisposed) {
            if (StackReplsManager.getGlobalRepl(project).exists(_.available)) {
              BrowseModuleComponent.findLibraryModuleIdentifiers(project, mn)
              // We have to wait for other requests which have more priority because those are on dispatch thread
              Thread.sleep(100)
            }
          }
        })
      }
    }
  }
}
