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

package intellij.haskell.util

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.{Project, ProjectManager}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import intellij.haskell.external.component.StackReplsComponentsManager
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.psi.{HaskellDeclarationElement, HaskellNamedElement, HaskellPsiUtil}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

object HaskellProjectUtil {

  def isHaskellStackProject(project: Project): Boolean = {
    HaskellModuleType.findHaskellProjectModules(project).nonEmpty && HaskellSdkType.getSdkHomePath(project) != null
  }

  def findProjectByLocationHash(locationHash: String): Project = {
    val project = ProjectManager.getInstance().getOpenProjects.find(_.getLocationHash == locationHash)
    project match {
      case Some(p) => p
      case None => throw new IllegalStateException(s"Could not find open project for project location hash: $locationHash")
    }
  }

  def findFile(filePath: String, project: Project): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    file.flatMap(f => Option(PsiManager.getInstance(project).findFile(f)).map(_.asInstanceOf[HaskellFile]))
  }

  def isProjectFile(psiFile: PsiFile): Boolean = {
    ProjectRootManager.getInstance(psiFile.getProject).getFileIndex.isInSourceContent(psiFile.getVirtualFile)
  }

  def isProjectFile(virtualFile: VirtualFile, project: Project): Boolean = {
    ProjectRootManager.getInstance(project).getFileIndex.isInSourceContent(virtualFile)
  }

  def isProjectTestFile(psiFile: PsiFile): Boolean = {
    ProjectRootManager.getInstance(psiFile.getProject).getFileIndex.isInTestSourceContent(psiFile.getVirtualFile)
  }

  def findFilesForModule(moduleName: String, project: Project): Iterable[HaskellFile] = {
    for {
      fp <- findFilePathsForModule(moduleName, project)
      f <- findFile(fp, project)
    } yield f
  }

  def findFilePathsForModule(moduleName: String, project: Project): Iterable[String] = {
    getFileNameAndDirNamesForModule(moduleName).map(names => {
      val (fileName, dirNames) = names
      val filePaths = for {
        file <- HaskellFileIndex.findFilesByName(project, fileName, GlobalSearchScope.allScope(project))
        if checkDirNames(file.getContainingDirectory, dirNames)
      } yield file.getVirtualFile.getPath

      if (filePaths.isEmpty) {
        if (StackReplsComponentsManager.findAvailableProjectModules(project).allModuleNames.exists(_ == moduleName)) {
          HaskellEditorUtil.showStatusBarInfoMessage(s"Could not find source code for `$moduleName`.'", project)
        } else {
          HaskellEditorUtil.showStatusBarInfoMessage(s"Could not find source code for `$moduleName`. Please use `Download Haskell library sources` in `Tools` from menu. Test dependencieas are not supported yet, see https://github.com/commercialhaskell/stack/issues/1919", project)
        }
        filePaths
      } else {
        filePaths
      }
    }).getOrElse(Iterable())
  }

  def findDeclarationElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    getHaskellFiles(project, includeNonProjectItems).flatMap(f => HaskellPsiUtil.findTopLevelDeclarationElements(f))
  }

  def findDeclarationElementsByName(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    val normalizedName = normalize(name)
    if (name.endsWith(" ")) {
      findDeclarationElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => ne.startsWith(normalizedName))
    } else {
      val names = normalizedName.split(' ')
      if (names.length == 1) {
        findDeclarationElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => ne.contains(names.head))
      } else {
        findDeclarationElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => names.exists(n => ne.contains(n)))
      }
    }
  }

  def findNamedElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    getHaskellFiles(project, includeNonProjectItems).flatMap(f => HaskellPsiUtil.findNamedElements(f))
  }

  def findNamedElementsByName(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    val normalizedName = normalize(name)
    if (name.endsWith(" ")) {
      findNamedElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => ne.startsWith(normalizedName))
    } else {
      val names = normalizedName.split(' ')
      if (names.length == 1) {
        findNamedElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => ne.contains(names.head))
      } else {
        findNamedElementsByConditionOnName(project, includeNonProjectItems, (ne: String) => names.exists(n => ne.contains(n)))
      }
    }
  }

  private def findDeclarationElementsByConditionOnName(project: Project, includeNonProjectItems: Boolean, condition: String => Boolean) = {
    findDeclarationElements(project, includeNonProjectItems).filter(de => de.getIdentifierElements.map(n => normalize(n.getName)).exists(n => condition(n)))
  }

  private def findNamedElementsByConditionOnName(project: Project, includeNonProjectItems: Boolean, condition: String => Boolean) = {
    findNamedElements(project, includeNonProjectItems).filter(ne => condition(normalize(ne.getName)))
  }

  private def getHaskellFiles(project: Project, includeNonProjectItems: Boolean) = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      ModuleManager.getInstance(project).getModules.map(GlobalSearchScope.moduleScope).reduce(_.uniteWith(_))
    }
    HaskellFileIndex.findHaskellFiles(project, scope)
  }

  private def normalize(name: String): String = {
    name.trim.toLowerCase
  }

  private def getFileNameAndDirNamesForModule(module: String) = {
    module.split('.').toList.reverse match {
      case n :: d => Some(n, d)
      case _ => HaskellNotificationGroup.logError(s"Could not determine directory names for $module"); None
    }
  }

  private def checkDirNames(dir: PsiDirectory, dirNames: List[String]): Boolean = {
    dirNames match {
      case dirName :: parentDirName =>
        if (dir.getName == dirName)
          checkDirNames(dir.getParentDirectory, parentDirName)
        else
          false
      case _ => true
    }
  }
}
