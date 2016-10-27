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
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiFile, PsiManager}
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

object HaskellProjectUtil {

  def isHaskellStackProject(project: Project): Boolean = {
    HaskellModuleType.findHaskellProjectModules(project).nonEmpty && HaskellSdkType.getSdkHomePath(project) != null
  }

  def findFile(filePath: String, project: Project): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    file.flatMap(f => Option(PsiManager.getInstance(project).findFile(f)).flatMap {
      case f: HaskellFile => Some(f)
      case _ => None
    })
  }

  def isLibraryFile(psiFile: PsiFile): Boolean = {
    isLibraryFile(psiFile.getContainingFile.getVirtualFile, psiFile.getProject)
  }

  def isLibraryFile(virtualFile: VirtualFile, project: Project): Boolean = {
    ProjectRootManager.getInstance(project).getFileIndex.isInLibrarySource(virtualFile)
  }

  def isProjectTestFile(psiFile: PsiFile): Boolean = {
    isProjectTestFile(psiFile.getOriginalFile.getVirtualFile, psiFile.getProject)
  }

  def isProjectTestFile(virtualFile: VirtualFile, project: Project): Boolean = {
    ProjectRootManager.getInstance(project).getFileIndex.isInTestSourceContent(virtualFile)
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
        if checkDirNames(file.getParent, dirNames)
      } yield file.getPath

      if (filePaths.isEmpty) {
        // Test dependencies are supported since Stack 1.2.1, see https://github.com/commercialhaskell/stack/issues/1919
        HaskellEditorUtil.showStatusBarInfoMessage(s"Could not find source code for `$moduleName`. Please use `Download Haskell library sources` in `Tools` from menu.", project)
        filePaths
      } else {
        filePaths
      }
    }).getOrElse(Iterable())
  }

  def findHaskellFiles(project: Project, includeNonProjectItems: Boolean) = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      ModuleManager.getInstance(project).getModules.map(GlobalSearchScope.moduleScope).reduce(_.uniteWith(_))
    }
    HaskellFileIndex.findFiles(project, scope)
  }

  private def getFileNameAndDirNamesForModule(module: String) = {
    module.split('.').toList.reverse match {
      case n :: d => Some(n, d)
      case _ => HaskellNotificationGroup.logError(s"Could not determine directory names for $module"); None
    }
  }

  private def checkDirNames(dir: VirtualFile, dirNames: List[String]): Boolean = {
    dirNames match {
      case dirName :: parentDirName =>
        if (dir.getName == dirName)
          checkDirNames(dir.getParent, parentDirName)
        else
          false
      case _ => true
    }
  }
}
