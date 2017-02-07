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

  def findDefinitionLocation(psiElement: PsiElement): Option[LocationInfo] = {
    // As a side effect we preload in background some extra info
    ApplicationManager.getApplication.invokeLater(() => {
      if (HaskellPsiUtil.findExpressionParent(psiElement).isDefined) {
        TypeInfoComponent.findTypeInfoForElement(psiElement)
      }
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
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() =
        if (!project.isDisposed) {
          val files = HaskellFileIndex.findProjectProductionPsiFiles(project)
          val importedLibraryModuleNames = files.flatMap(pf => HaskellPsiUtil.findImportDeclarations(pf).flatMap(_.getModuleName))
          importedLibraryModuleNames.foreach(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn))
        }
    })
  }
}
