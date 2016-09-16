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

import java.util.concurrent.TimeUnit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.psi.HaskellPsiUtil

import scala.collection.mutable

object StackReplsComponentsManager {

  private[this] final val PreloadTimeoutInMinutes = 10

  private[this] val isCachePreloaded = mutable.Map[Project, Boolean]()

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Some(a) else None
  }

  def findImportedModuleIdentifiers(project: Project, moduleName: String, psiFile: PsiFile) = {
    findIfCacheIsPreloaded(project, BrowseModuleComponent.findImportedModuleIdentifiers(project, moduleName, Some(psiFile)))
  }

  def findAllTopLevelModuleIdentifiers(project: Project, moduleName: String, psiFile: PsiFile) = {
    findIfCacheIsPreloaded(project, BrowseModuleComponent.findAllTopLevelModuleIdentifiers(project, moduleName, Some(psiFile)))
  }

  def findDefinitionLocation(psiElement: PsiElement): Option[DefinitionLocation] = {
    DefinitionLocationComponent.findDefinitionLocation(psiElement)
  }

  def findNameInfo(psiElement: PsiElement): Iterable[NameInfo] = {
    NameInfoComponent.findNameInfo(psiElement)
  }

  def findAvailableProjectModules(project: Project): ProjectModules = {
    ProjectModulesComponent.findAvailableModules(project)
  }

  def findGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    findIfCacheIsPreloaded(project, GlobalProjectInfoComponent.findGlobalProjectInfo(project))
  }

  def loadHaskellFile(psiFile: PsiFile, refreshCache: Boolean): LoadResult = {
    LoadComponent.load(psiFile, refreshCache, refreshCachesAfterLoad(psiFile))
  }

  def invalidateModuleIdentifierCaches(project: Project) = {
    isCachePreloaded.put(project, false)
    BrowseModuleComponent.invalidate()
    ProjectModulesComponent.invalidate(project)
    GlobalProjectInfoComponent.invalidate(project)
  }

  def preloadModuleIdentifiersCaches(project: Project) = {
    val libraryModuleIdentifiersFuture = ApplicationManager.getApplication.executeOnPooledThread {
      new Runnable {
        override def run(): Unit = {
          preloadAllLibraryModuleIdentifiers(project)
        }
      }
    }
    val projectModuleIdentifiersFuture = ApplicationManager.getApplication.executeOnPooledThread {
      new Runnable {
        override def run(): Unit = {
          preloadAllProjectModuleIdentifiers(project)
        }
      }
    }
    libraryModuleIdentifiersFuture.get(PreloadTimeoutInMinutes, TimeUnit.MINUTES)
    projectModuleIdentifiersFuture.get(PreloadTimeoutInMinutes, TimeUnit.MINUTES)
    isCachePreloaded.put(project, true)
    HaskellAnnotator.restartDaemonCodeAnalyzerForOpenFiles(project)
  }

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForElement(psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    TypeInfoComponent.findTypeInfoForSelection(psiFile, selectionModel)
  }

  private def refreshCachesAfterLoad(psiFile: PsiFile): Unit = {
    val project = psiFile.getProject

    findModuleName(psiFile).foreach(BrowseModuleComponent.refreshForModule(project, _))

    NameInfoComponent.refresh(psiFile)
    ProjectModulesComponent.refresh(project)

    TypeInfoComponent.invalidate(psiFile)
    DefinitionLocationComponent.invalidate(psiFile)
  }

  private def preloadAllLibraryModuleIdentifiers(project: Project): Unit = {
    val libraryModuleNames = GlobalProjectInfoComponent.findGlobalProjectInfo(project).availableInTestLibraryModuleNames
    libraryModuleNames.flatMap(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn, None))
  }

  private def preloadAllProjectModuleIdentifiers(project: Project): Unit = {
    val prodModuleNames = ProjectModulesComponent.findAvailableModules(project).prodModuleNames
    prodModuleNames.flatMap(mn => BrowseModuleComponent.findImportedModuleIdentifiers(project, mn, None))
  }

  private def findIfCacheIsPreloaded[A](project: Project, f: => Iterable[A]) = {
    isCachePreloaded.getOrElse(project, false).option(f).getOrElse(Iterable())
  }

  private def findIfCacheIsPreloaded[A](project: Project, f: => A) = {
    isCachePreloaded.getOrElse(project, false).option(f)
  }

  private def findModuleName(psiFile: PsiFile) = {
    ApplicationManager.getApplication.runReadAction {
      new Computable[Option[String]] {
        override def compute(): Option[String] = {
          HaskellPsiUtil.findModuleName(psiFile)
        }
      }
    }
  }
}
