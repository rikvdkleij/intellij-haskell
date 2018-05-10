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

package intellij.haskell.external.component

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal.CabalInfo
import intellij.haskell.external.component.DefinitionLocationComponent.DefinitionLocationResult
import intellij.haskell.external.component.NameInfoComponentResult.NameInfoResult
import intellij.haskell.external.component.TypeInfoComponentResult.TypeInfoResult
import intellij.haskell.external.execution.CompilationResult
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.psi.{HaskellNamedElement, HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.HaskellFileNameIndex
import intellij.haskell.util.{HaskellProjectUtil, ScalaUtil}

object HaskellComponentsManager {

  def findPreloadedModuleIdentifiers(project: Project): Iterable[ModuleIdentifier] = {
    val moduleNames = BrowseModuleComponent.findModuleNamesInCache(project)
    moduleNames.flatMap(mn => findExportedModuleIdentifiers(project, mn))
  }

  def findExportedModuleIdentifiers(project: Project, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findModuleIdentifiers(project, moduleName, None)
  }

  def findLocalModuleIdentifiers(psiFile: PsiFile, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findModuleIdentifiers(psiFile.getProject, moduleName, Some(psiFile))
  }

  def findDefinitionLocation(namedElement: HaskellNamedElement): Option[DefinitionLocationResult] = {
    DefinitionLocationComponent.findDefinitionLocation(namedElement)
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

  def findAvailableModuleNames(psiFile: PsiFile): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableModuleNames(psiFile)
  }

  def findAvailableStackTargetProjectModuleNames(psiFile: PsiFile): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableStackTargetProjectModuleNames(psiFile)
  }

  def findStackComponentGlobalInfo(psiFile: PsiFile): Option[StackComponentGlobalInfo] = {
    StackComponentGlobalInfoComponent.findStackComponentGlobalInfo(psiFile)
  }

  def findStackComponentInfo(psiFile: PsiFile): Option[StackComponentInfo] = {
    HaskellProjectFileInfoComponent.findHaskellProjectFileInfo(psiFile).map(_.stackComponentInfo)
  }

  def getSupportedLanguageExtension(project: Project): Iterable[String] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.supportedLanguageExtensions).getOrElse(Iterable())
  }

  def getAvailablePackages(project: Project): Iterable[String] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.availablePackageNames).getOrElse(Iterable())
  }

  def findProjectPackageNames(project: Project): Option[Iterable[String]] = {
    StackReplsManager.getReplsManager(project).map(_.cabalInfos.map(_.packageName))
  }

  def findCabalInfos(project: Project): Iterable[CabalInfo] = {
    StackReplsManager.getReplsManager(project).map(_.cabalInfos).getOrElse(Iterable())
  }

  def loadHaskellFile(psiFile: PsiFile, psiElement: Option[PsiElement]): Option[CompilationResult] = {
    LoadComponent.load(psiFile, psiElement)
  }

  def invalidateHaskellFileInfoCache(psiFile: PsiFile): Unit = {
    HaskellProjectFileInfoComponent.invalidate(psiFile)
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to invalidate cache")
    GlobalProjectInfoComponent.invalidate(project)
    StackComponentGlobalInfoComponent.invalidate(project)
    HaskellProjectFileInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    DefinitionLocationComponent.invalidateAll(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with invalidating cache")
  }

  def preloadLibraryIdentifiersCaches(project: Project): Unit = {
    // FIXME For now no preload of libraries
//    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library cache")
//    preloadLibraryIdentifiers(project)
//    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library cache")
  }

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfoResult] = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfoResult] = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def preloadLibraryIdentifiers(project: Project): Unit = {
    if (!project.isDisposed) {
      if (!project.isDisposed) {
        DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(BrowseModuleComponent.findModuleIdentifiers(project, HaskellProjectUtil.Prelude, None), null))
      }

      if (!project.isDisposed) {
        val projectHaskellFiles =
          DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(
            if (project.isDisposed) {
              Iterable()
            } else {
              HaskellFileNameIndex.findProjectProductionHaskellFiles(project)
            }
          ))

        val importedLibraryModuleNames =
          DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(
            projectHaskellFiles.flatMap(f => {
              if (project.isDisposed) {
                Iterable[String]()
              } else {
                val libraryModuleNames = findStackComponentGlobalInfo(f).map(_.availableLibraryModuleNames).getOrElse(Iterable())
                HaskellPsiUtil.findImportDeclarations(f).flatMap(_.getModuleName.filter(_.nonEmpty)).filter(mn => libraryModuleNames.exists(_ == mn))
                  .toSeq.distinct.filterNot(a => a == HaskellProjectUtil.Prelude)
              }

            })
          ))

        if (!project.isDisposed) {
          importedLibraryModuleNames.foreach(mn => {
            if (!project.isDisposed) {
              if (StackReplsManager.getGlobalRepl(project).exists(_.available)) {
                DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(BrowseModuleComponent.findModuleIdentifiers(project, mn, None)))
                // We have to wait for other requests which have more priority because those are on dispatch thread
                Thread.sleep(100)
              }
            }
          })
        }
      }
    }
  }
}
