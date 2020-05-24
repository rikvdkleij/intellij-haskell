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

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.component.DefinitionLocationComponent.DefinitionLocationResult
import intellij.haskell.external.component.NameInfoComponentResult.NameInfoResult
import intellij.haskell.external.component.TypeInfoComponentResult.TypeInfoResult
import intellij.haskell.external.execution.CompilationResult
import intellij.haskell.external.repl.StackRepl.StanzaType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.{HaskellFileIndex, HaskellModuleNameIndex}
import intellij.haskell.util.{ApplicationUtil, GhcVersion, HaskellProjectUtil, ScalaFutureUtil}

import scala.concurrent._

object HaskellComponentsManager {

  case class StackComponentInfo(module: Module, modulePath: String, packageName: String, target: String, stanzaType: StanzaType, sourceDirs: Seq[String],
                                mainIs: Option[String], isImplicitPreludeActive: Boolean, buildDepends: Seq[String], exposedModuleNames: Seq[String] = Seq.empty)

  def findModuleIdentifiersInCache(project: Project): Iterable[ModuleIdentifier] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val f = Future {
      blocking {
        BrowseModuleComponent.findModuleIdentifiersInCache(project)
      }
    }
    ScalaFutureUtil.waitForValue(project, f, "find module identifiers in cache") match {
      case Some(ids) => ids
      case None => Iterable()
    }
  }

  def clearLoadedModule(psiFile: PsiFile): Unit = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.foreach(_.clearLoadedModule())
  }

  def findModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    BrowseModuleComponent.findModuleIdentifiers(project, moduleName)
  }

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, importQualifier: Option[String]): DefinitionLocationResult = {
    DefinitionLocationComponent.findDefinitionLocation(psiFile, qualifiedNameElement, importQualifier)
  }

  def findNameInfo(psiElement: PsiElement): NameInfoResult = {
    NameInfoComponent.findNameInfo(psiElement)
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
    HaskellModuleInfoComponent.findHaskellProjectFileInfo(psiFile)
  }

  def findStackComponentInfo(project: Project, filePath: String): Option[StackComponentInfo] = {
    HaskellModuleInfoComponent.findHaskellProjectFileInfo(project, filePath)
  }

  def getGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project)
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

  def findProjectPackageNames(project: Project): Option[Iterable[String]] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (_, ci) => ci.packageName })
  }

  def findCabalInfos(project: Project): Iterable[CabalInfo] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (_, ci) => ci }).getOrElse(Iterable())
  }

  def loadHaskellFile(psiFile: PsiFile, fileModified: Boolean): Option[CompilationResult] = {
    LoadComponent.load(psiFile, fileModified)
  }

  def invalidateFileInfos(psiFile: PsiFile): Unit = {
    HaskellModuleInfoComponent.invalidate(psiFile)
  }

  def findProjectModulePackageNames(project: Project): Seq[(Module, String)] = {
    findStackComponentInfos(project).map(info => (info.module, info.packageName)).distinct
  }

  def invalidateDefinitionLocations(project: Project): Unit = {
    DefinitionLocationComponent.invalidate(project)
  }

  def findLibraryPackageInfos(project: Project): Seq[PackageInfo] = {
    LibraryPackageInfoComponent.libraryPackageInfos(project).toSeq
  }

  def invalidateBrowseInfo(project: Project, moduleNames: Seq[String]): Unit = {
    BrowseModuleComponent.invalidateModuleNames(project, moduleNames)
  }

  def findStackComponentInfos(project: Project): Seq[StackComponentInfo] = {
    StackReplsManager.getReplsManager(project).map(_.stackComponentInfos.toSeq).getOrElse(Seq())
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to invalidate cache")
    GlobalProjectInfoComponent.invalidate(project)
    LibraryPackageInfoComponent.invalidate(project)
    HaskellModuleInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    DefinitionLocationComponent.invalidateAll(project)
    TypeInfoComponent.invalidateAll(project)
    HaskellPsiUtil.invalidateAllModuleNames(project)
    LibraryPackageInfoComponent.invalidate(project)
    HaskellModuleNameIndex.invalidate(project)
    FileModuleIdentifiers.invalidateAll(project)
    StackComponentGlobalInfoComponent.invalidate(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with invalidating cache")
  }

  def preloadLibraryIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library identifiers cache")
    preloadLibraryIdentifiers(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library identifiers cache")
  }

  def preloadAllLibraryIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload all library identifiers cache")
    preloadAllLibraryIdentifiers(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading all library identifiers cache")
  }

  def preloadStackComponentInfoCache(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload stack component info cache")
    preloadStackComponentInfos(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading stack component info cache")
  }

  def preloadLibraryFilesCache(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library files cache")
    preloadLibraryFiles(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library files cache")
  }

  def findTypeInfoForElement(psiElement: PsiElement): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def preloadStackComponentInfos(project: Project): Unit = {
    if (!project.isDisposed) {
      findStackComponentInfos(project).foreach { info =>
        findStackComponentGlobalInfo(info)
        val projectModuleNames = AvailableModuleNamesComponent.findAvailableProjectModuleNames(info)
        HaskellModuleNameIndex.fillCache(project, projectModuleNames)
      }
    }
  }

  private def preloadLibraryFiles(project: Project): Unit = {
    if (!project.isDisposed) {
      val libraryPackageInfos = LibraryPackageInfoComponent.libraryPackageInfos(project)
      HaskellModuleNameIndex.fillCache(project, libraryPackageInfos.flatMap(libraryModuleNames => libraryModuleNames.exposedModuleNames ++ libraryModuleNames.hiddenModuleNames))
    }
  }

  private def preloadLibraryIdentifiers(project: Project): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!project.isDisposed) {
      BrowseModuleComponent.findModuleIdentifiers(project, HaskellProjectUtil.Prelude)
    }

    if (!project.isDisposed) {
      val projectHaskellFiles = ApplicationUtil.runReadActionWithFileAccess(project, HaskellFileIndex.findProjectHaskellFiles(project), "Find Haskell project files").toOption.getOrElse(Iterable())

      val componentInfos = projectHaskellFiles.flatMap(f => HaskellComponentsManager.findStackComponentInfo(f)).toSeq.distinct

      val importedLibraryModuleNames =
        projectHaskellFiles.flatMap(f => {
          if (project.isDisposed) {
            Iterable()
          } else {
            val packageInfos = componentInfos.flatMap(HaskellComponentsManager.findStackComponentGlobalInfo).flatMap(_.packageInfos)

            val exposedLibraryModuleNames = packageInfos.flatMap(_.exposedModuleNames).distinct
            val importDeclarations = ApplicationUtil.runReadActionWithFileAccess(project, HaskellPsiUtil.findImportDeclarations(f), "In preloadLibraryIdentifiers findImportDeclarations").toOption.getOrElse(Iterable())
            importDeclarations.flatMap(id => ApplicationUtil.runReadAction(id.getModuleName, Some(project))).filter(mn => exposedLibraryModuleNames.contains(mn)).filterNot(_ == HaskellProjectUtil.Prelude)
          }
        })

      if (!project.isDisposed) {
        if (StackReplsManager.getGlobalRepl(project).exists(_.available)) {
          importedLibraryModuleNames.toSeq.distinct.foreach(mn => {
            if (!project.isDisposed) {
              BrowseModuleComponent.findModuleIdentifiersSync(project, mn)
            }
          })
        }
      }
    }
  }

  private def preloadAllLibraryIdentifiers(project: Project): Unit = {
    if (!project.isDisposed) {
      val componentInfos = findStackComponentInfos(project)
      val packageInfos = componentInfos.flatMap(info => findStackComponentGlobalInfo(info).map(_.packageInfos).getOrElse(Seq())).distinct

      if (!project.isDisposed) {
        if (StackReplsManager.getGlobalRepl(project).exists(_.available)) {
          packageInfos.flatMap(_.exposedModuleNames).distinct.foreach(mn => {
            if (!project.isDisposed) {
              BrowseModuleComponent.findModuleIdentifiersSync(project, mn)
              // We have to wait for other requests which have more priority because those are on dispatch thread
              Thread.sleep(100)
            }
          })
        }
      }
    }
  }
}
