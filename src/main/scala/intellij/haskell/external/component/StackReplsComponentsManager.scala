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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileIndex

object StackReplsComponentsManager {

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Some(a) else None
  }

  def findAvailableModuleNamesForModuleIdentifiers(project: Project): Iterable[String] = {
    BrowseModuleComponent.findModuleNamesInCache(project)
  }

  def findImportedModuleIdentifiers(project: Project, moduleName: String): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findImportedModuleIdentifiers(project, moduleName)
  }

  def findAllTopLevelModuleIdentifiers(project: Project, moduleName: String, psiFile: PsiFile): Iterable[ModuleIdentifier] = {
    BrowseModuleComponent.findAllTopLevelModuleIdentifiers(project, moduleName, psiFile)
  }

  def findDefinitionLocation(psiElement: PsiElement): Option[DefinitionLocation] = {
    // As a side effect we preload in background some extra info
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = {
        if (HaskellPsiUtil.findExpressionParent(psiElement).isDefined) {
          TypeInfoComponent.findTypeInfoForElement(psiElement)
        }
        NameInfoComponent.findNameInfo(psiElement)
      }
    })

    DefinitionLocationComponent.findDefinitionLocation(psiElement)
  }

  def findNameInfo(psiElement: PsiElement): Iterable[NameInfo] = {
    NameInfoComponent.findNameInfo(psiElement)
  }

  def findAvailableModuleNames(psiFile: PsiFile): Stream[String] = {
    AvailableModuleNamesComponent.findAvailableModuleNames(psiFile)
  }

  def findGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project)
  }

  def loadHaskellFile(psiFile: PsiFile, refreshCache: Boolean): LoadResult = {
    LoadComponent.load(psiFile, refreshCache)
  }

  def invalidateModuleIdentifierCaches(project: Project): Unit = {
    BrowseModuleComponent.invalidate(project)
    GlobalProjectInfoComponent.invalidate(project)
  }

  def preloadModuleIdentifiersCaches(project: Project): Unit = {
    preloadAllLibraryModuleIdentifiers(project)
    HaskellAnnotator.restartDaemonCodeAnalyzerForOpenFiles(project)
  }

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def preloadAllLibraryModuleIdentifiers(project: Project): Unit = {
    GlobalProjectInfoComponent.findGlobalProjectInfo(project) match {
      case Some(info) =>
        ApplicationManager.getApplication.invokeLater(new Runnable {
          override def run(): Unit = {
            val files = HaskellFileIndex.findProjectProductionPsiFiles(project)
            val libraryModuleNames = files.flatMap(pf => HaskellPsiUtil.findImportDeclarations(pf).flatMap(_.getModuleName))
            libraryModuleNames.foreach(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn))
          }
        })

        info.allAvailableLibraryModuleNames.foreach { mn =>
          Thread.sleep(100) // Otherwise it will make IDE unresponsive
          BrowseModuleComponent.findImportedModuleIdentifiers(project, mn)
        }
      case _ => HaskellNotificationGroup.notifyBalloonWarning("Could not preload library identifiers cache because could not obtain global project info")
    }
  }
}
