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

import java.util.concurrent.{Executors, TimeUnit}

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.module.Module
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
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.HaskellFileIndex
import intellij.haskell.util.{HaskellProjectUtil, ScalaUtil}

import scala.concurrent._
import scala.concurrent.duration.Duration

object HaskellComponentsManager {

  private final val Timeout = Duration.create(100, TimeUnit.MILLISECONDS)

  def findPreloadedModuleIdentifiers(project: Project)(implicit ec: ExecutionContext): Iterable[ModuleIdentifier] = {
    val moduleNames = BrowseModuleComponent.findModuleNamesInCache(project)
    moduleNames.flatMap(mn => {
      try {
        Await.result(findModuleIdentifiers(project, mn), Timeout)
      } catch {
        case _: TimeoutException => Iterable()
      }
    })
  }

  def clearLoadedModule(psiFile: PsiFile): Unit = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.foreach(_.clearLoadedModule())
  }

  def isReplBusy(psiFile: PsiFile): Boolean = {
    LoadComponent.isBusy(psiFile)
  }

  def findModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    BrowseModuleComponent.findModuleIdentifiers(project, moduleName, None)
  }

  def findLocalModuleIdentifiers(project: Project, psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    BrowseModuleComponent.findModuleIdentifiers(project, moduleName, Some(psiFile))
  }

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, isCurrentFile: Boolean = false): DefinitionLocationResult = {
    DefinitionLocationComponent.findDefinitionLocation(psiFile, qualifiedNameElement, isCurrentFile)
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

  def findAvailableModuleNamesWithIndex(psiFile: PsiFile): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableModuleNamesWithIndex(psiFile)
  }

  def findAvailableModuleLibraryModuleNamesWithIndex(module: Module): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableLibraryModuleNamesWithIndex(module)
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

  def getAvailablePackages(project: Project): Iterable[String] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.availablePackageNames).getOrElse(Iterable())
  }

  def findProjectPackageNames(project: Project): Option[Iterable[String]] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (_, ci) => ci.packageName })
  }

  def findCabalInfos(project: Project): Iterable[CabalInfo] = {
    StackReplsManager.getReplsManager(project).map(_.moduleCabalInfos.map { case (m, ci) => ci }).getOrElse(Iterable())
  }

  def loadHaskellFile(psiFile: PsiFile, psiElement: Option[PsiElement]): Option[CompilationResult] = {
    LoadComponent.load(psiFile, psiElement)
  }

  def invalidateHaskellFileInfoCache(psiFile: PsiFile): Unit = {
    HaskellProjectFileInfoComponent.invalidate(psiFile)
  }

  def invalidateLocationAndTypeInfo(psiFile: PsiFile): Unit = {
    DefinitionLocationComponent.invalidate(psiFile)
    TypeInfoComponent.invalidate(psiFile)
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to invalidate cache")
    GlobalProjectInfoComponent.invalidate(project)
    StackComponentGlobalInfoComponent.invalidate(project)
    HaskellProjectFileInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    DefinitionLocationComponent.invalidateAll(project)
    TypeInfoComponent.invalidateAll(project)
    HaskellPsiUtil.invalidateAllModuleNames(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with invalidating cache")
  }

  def preloadLibraryIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload library cache")
    preloadLibraryIdentifiers(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading library cache")
  }

  def findTypeInfoForElement(psiElement: PsiElement): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): TypeInfoResult = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def preloadLibraryIdentifiers(project: Project): Unit = {
    val ExecutorService = Executors.newCachedThreadPool()
    implicit val ExecContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(ExecutorService)

    if (!project.isDisposed) {
      DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(BrowseModuleComponent.findModuleIdentifiers(project, HaskellProjectUtil.Prelude, None)))
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

      val importedLibraryModuleNames =
        projectHaskellFiles.flatMap(f => {
          if (project.isDisposed) {
            Iterable()
          } else {
            // We have to wait for other requests which have more priority because those are on dispatch thread
            Thread.sleep(100)

            val libraryModuleNames = for {
              stackComponentInfo <- HaskellComponentsManager.findStackComponentInfo(f)
              globalProjectInfo <- HaskellComponentsManager.findStackComponentGlobalInfo(stackComponentInfo) // It can happen that REPL is busy and there is no globalProjectInfo available
            } yield globalProjectInfo.availableLibraryModuleNames

            libraryModuleNames match {
              case Some(moduleNames) =>
                DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(
                  HaskellPsiUtil.findImportDeclarations(f).flatMap(_.getModuleName.filter(_.nonEmpty)).filter(mn => moduleNames.exists(_ == mn))
                    .toSeq.distinct.filterNot(a => a == HaskellProjectUtil.Prelude)))
              case None => Iterable()
            }
          }
        })

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
