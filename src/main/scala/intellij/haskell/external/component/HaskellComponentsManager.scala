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

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.psi.HaskellNamedElement
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

object HaskellComponentsManager {

  def findPreloadedModuleIdentifiers(project: Project): Iterable[ModuleIdentifier] = {
    val moduleNames = BrowseModuleComponent.findModuleNamesInCache(project)
    moduleNames.flatMap(mn => findImportedModuleIdentifiers(project, mn))
  }

  def findImportedModuleIdentifiers(project: Project, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findImportedModuleIdentifiers(project, moduleName)
  }

  def findAllTopLevelModuleIdentifiers(project: Project, psiFile: PsiFile, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findAllTopLevelModuleIdentifiers(project, moduleName, psiFile)
  }

  def findDefinitionLocation(namedElement: HaskellNamedElement): Option[LocationInfo] = {
    DefinitionLocationComponent.findDefinitionLocation(namedElement)
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

  def invalidateModuleFileCache(project: Project): Unit = {
    ModuleFileComponent.invalidate(project)
  }

  def invalidateGlobalCaches(project: Project): Unit = {
    GlobalProjectInfoComponent.invalidate(project)
    BrowseModuleComponent.invalidate(project)
    NameInfoComponent.invalidateAll(project)
    ModuleFileComponent.invalidate(project)
  }

  def preloadModuleIdentifiersCaches(project: Project): Unit = {
    HaskellNotificationGroup.logInfoEvent(project, "Start to preload cache")
    preloadLibraryIdentifiers(project)
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

  private def preloadLibraryIdentifiers(project: Project): Unit = {
    if (!project.isDisposed) {
      val projectInfo = findGlobalProjectInfo(project)
      if (!project.isDisposed) {
        BrowseModuleComponent.findImportedModuleIdentifiers(project, "Prelude")
        Thread.sleep(1000)
      }
      if (!project.isDisposed) {
        projectInfo.foreach(_.allAvailableLibraryModuleNames.find(_ == "Protolude").foreach(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn)))
        Thread.sleep(1000)
      }
      if (!project.isDisposed) {
        projectInfo.foreach(_.allAvailableLibraryModuleNames.foreach(mn => {
          if (!project.isDisposed) {
            BrowseModuleComponent.findImportedModuleIdentifiers(project, mn)
            Thread.sleep(1000)
          }
        }))
      }
    }
  }
}
