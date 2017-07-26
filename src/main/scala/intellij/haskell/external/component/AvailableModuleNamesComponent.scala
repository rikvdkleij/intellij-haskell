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
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

import scala.collection.JavaConverters._

private[component] object AvailableModuleNamesComponent {

  def findAvailableModuleNames(psiFile: PsiFile): Iterable[String] = {
    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
    val project = psiFile.getProject
    val stackComponentModuleNames = stackComponentInfo.map(info => findModuleNamesInDirectories(project, info.sourceDirs.flatMap(d => HaskellFileUtil.findDirectory(d, project)))).getOrElse(Stream())
    val libraryModuleNames = findAvailableLibraryModuleNames(psiFile)

    stackComponentModuleNames ++ libraryModuleNames
  }

  private def findAvailableLibraryModuleNames(psiFile: PsiFile): Iterable[String] = {
    HaskellComponentsManager.findStackComponentGlobalInfo(psiFile).map(_.availableLibraryModuleNames).getOrElse(Iterable())
  }

  private def findModuleNamesInDirectories(project: Project, directories: Seq[VirtualFile]): Iterable[String] = {
    val files = FileTypeIndex.getFiles(HaskellFileType.Instance, GlobalSearchScopesCore.directoriesScope(project, true, directories: _*)).asScala
    files.flatMap(f => {
      for {
        hf <- HaskellFileUtil.convertToHaskellFile(project, f)
        m <- HaskellPsiUtil.findModuleName(hf, runInRead = true)
      } yield m
    })
  }
}
