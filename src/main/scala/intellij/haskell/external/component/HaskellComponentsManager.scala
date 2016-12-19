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

package intellij.haskell.external.component

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.index.HaskellFileIndex
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

object HaskellComponentsManager {

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Some(a) else None
  }

  def findPreloadedModuleIdentifiers(project: Project): Iterable[ModuleIdentifier] = {
    val moduleNames = BrowseModuleComponent.findModuleNamesInCache(project)
    moduleNames.flatMap(mn => findImportedModuleIdentifiers(project, mn))
  }

  def findImportedModuleIdentifiers(project: Project, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findImportedModuleIdentifiers(project, moduleName)
  }

  def findAllTopLevelModuleIdentifiers(project: Project, moduleName: String, psiFile: PsiFile): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findAllTopLevelModuleIdentifiers(project, moduleName, psiFile)
  }

  def findDefinitionLocation(psiElement: PsiElement): Option[DefinitionLocation] = {
    // As a side effect we preload in background some extra info
    ApplicationManager.getApplication.invokeLater(() => {
      if (HaskellPsiUtil.findExpressionParent(psiElement).isDefined) {
        TypeInfoComponent.findTypeInfoForElement(psiElement)
      }
      NameInfoComponent.findNameInfo(psiElement)
    })

    DefinitionLocationComponent.findDefinitionLocation(psiElement)
  }

  def findNameInfo(psiElement: PsiElement): Iterable[NameInfo] = {
    NameInfoComponent.findNameInfo(psiElement)
  }

  def findNameInfoByModuleAndName(project: Project, moduleName: String, name: String): Iterable[NameInfo] = {
    NameInfoComponent.findNameInfoByModuleAndName(project, moduleName, name)
  }

  def findAvailableModuleNames(psiFile: PsiFile): Iterable[String] = {
    AvailableModuleNamesComponent.findAvailableModuleNames(psiFile)
  }

  def findGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project)
  }

  def loadHaskellFile(psiFile: PsiFile): LoadResult = {
    LoadComponent.load(psiFile)
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    GlobalProjectInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    ModuleFileComponent.invalidate(project)
  }

  def preloadModuleIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload cache")
    preloadAllLibraryModuleIdentifiers(project)
    HaskellAnnotator.restartDaemonCodeAnalyzerForOpenFiles(project)
    HaskellNotificationGroup.logInfoEvent(project, "Finished with preloading cache")
  }

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  def findHaskellFiles(project: Project, moduleName: String): Iterable[HaskellFile] = {
    ModuleFileComponent.findHaskellFiles(project, moduleName)
  }

  private def preloadAllLibraryModuleIdentifiers(project: Project): Unit = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project) match {
      case Some(info) =>
        ApplicationManager.getApplication.invokeLater(() => {
          val files = HaskellFileIndex.findProjectProductionPsiFiles(project)
          val libraryModuleNames = files.flatMap(pf => HaskellPsiUtil.findImportDeclarations(pf).flatMap(_.getModuleName))
          libraryModuleNames.foreach(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn))
        })

        info.allAvailableLibraryModuleNames.foreach { mn =>
          if (!project.isDisposed) {
            Thread.sleep(200) // Otherwise it will make IDE unresponsive
            BrowseModuleComponent.findImportedModuleIdentifiers(project, mn)
          }
        }
      case _ => HaskellNotificationGroup.logWarningBalloonEvent(project, "Could not preload library identifiers cache because could not obtain global project info")
    }
  }
}
