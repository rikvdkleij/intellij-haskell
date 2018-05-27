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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScopesCore}
import intellij.haskell.HaskellFileType
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.util.index.HaskellFilePathIndex

import scala.collection.JavaConverters._

// TODO Consider to use cache with expiring
private[component] object AvailableModuleNamesComponent {

  def findAvailableModuleNamesWithIndex(psiFile: PsiFile): Iterable[String] = {
    val libraryModuleNames = findAvailableLibraryModuleNames(psiFile)
    findAvailableProjectModuleNamesWithIndex(psiFile) ++ libraryModuleNames
  }

  def findAvailableProjectModuleNamesWithIndex(psiFile: PsiFile): Iterable[String] = {
    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
    val project = psiFile.getProject
    stackComponentInfo.map(info => findModuleNamesInDirectories(project, info.sourceDirs.flatMap(d => HaskellFileUtil.findDirectory(d, project)))).getOrElse(Iterable())
  }

  private def findAvailableLibraryModuleNames(psiFile: PsiFile): Iterable[String] = {
    HaskellComponentsManager.findStackComponentGlobalInfo(psiFile).map(_.availableLibraryModuleNames).getOrElse(Iterable())
  }

  private def findModuleNamesInDirectories(project: Project, directories: Seq[VirtualFile]): Iterable[String] = {
    for {
      vf <- FileTypeIndex.getFiles(HaskellFileType.Instance, GlobalSearchScopesCore.directoriesScope(project, true, directories: _*)).asScala
      hf <- HaskellFileUtil.convertToHaskellFile(project, vf)
      mn <- HaskellFilePathIndex.findModuleName(hf, GlobalSearchScopesCore.directoriesScope(project, true, directories: _*))
    } yield mn
  }
}
