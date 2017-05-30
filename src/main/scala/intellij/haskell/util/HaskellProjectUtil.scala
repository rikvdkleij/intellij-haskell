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

import java.io.File

import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiFile, PsiManager}
import intellij.haskell.HaskellFile
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType

object HaskellProjectUtil {

  final val Prelude = "Prelude"
  final val Protolude = "Protolude"

  def isValidHaskellProject(project: Project, notifyNoSdk: Boolean): Boolean = {
    val stackPath = HaskellSdkType.getStackPath(project, notifyNoSdk)
    isHaskellProject(project) && stackPath.isDefined
  }

  def isHaskellProject(project: Project): Boolean = {
    HaskellModuleType.findHaskellProjectModules(project).nonEmpty
  }

  def findFile(filePath: String, project: Project): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    file.flatMap(f => Option(PsiManager.getInstance(project).findFile(f)).flatMap {
      case f: HaskellFile => Some(f)
      case _ => None
    })
  }

  def isLibraryFile(psiFile: PsiFile): Option[Boolean] = {
    isLibraryFile(psiFile.getContainingFile.getVirtualFile, psiFile.getProject)
  }

  def isLibraryFile(virtualFile: VirtualFile, project: Project): Option[Boolean] = {
    getProjectRootManager(project).map(_.getFileIndex.isInLibrarySource(virtualFile))
  }

  def isProjectTestFile(psiFile: PsiFile): Option[Boolean] = {
    isProjectTestFile(psiFile.getOriginalFile.getVirtualFile, psiFile.getProject)
  }

  def isProjectTestFile(virtualFile: VirtualFile, project: Project): Option[Boolean] = {
    getProjectRootManager(project).map(_.getFileIndex.isInTestSourceContent(virtualFile))
  }

  def isProjectFile(psiFile: PsiFile): Option[Boolean] = {
    isProjectFile(psiFile.getOriginalFile.getVirtualFile, psiFile.getProject)
  }

  def isProjectFile(virtualFile: VirtualFile, project: Project): Option[Boolean] = {
    getProjectRootManager(project).map(_.getFileIndex.isInSource(virtualFile))
  }

  def findCabalPackageName(project: Project): Option[String] = {
    findCabalPackageName(new File(project.getBasePath))
  }

  def findCabalPackageName(directory: File): Option[String] = {
    findCabalFile(directory).map(_.getName.replaceFirst(".cabal", ""))
  }

  def findCabalFile(project: Project): Option[File] = {
    findCabalFile(new File(project.getBasePath))
  }

  def findCabalFile(directory: File): Option[File] = {
    directory.listFiles.find(_.getName.endsWith(".cabal"))
  }

  def findStackFile(directory: File): Option[File] = {
    directory.listFiles.find(_.getName == "stack.yaml")
  }

  def findStackFile(project: Project): Option[File] = {
    findStackFile(new File(project.getBasePath))
  }

  def getProjectModulesSearchScope(project: Project): GlobalSearchScope = {
    getProjectModules(project).map(GlobalSearchScope.moduleScope).reduce(_.uniteWith(_))
  }

  def getSearchScope(project: Project, includeNonProjectItems: Boolean): GlobalSearchScope = {
    if (includeNonProjectItems) GlobalSearchScope.allScope(project) else HaskellProjectUtil.getProjectModulesSearchScope(project)
  }

  import ScalaUtil._

  def getProjectRootManager(project: Project): Option[ProjectRootManager] = {
    project.isDisposed.optionNot(ProjectRootManager.getInstance(project))
  }

  def getModuleManager(project: Project): Option[ModuleManager] = {
    project.isDisposed.optionNot(ModuleManager.getInstance(project))
  }

  def getProjectModules(project: Project): Array[Module] = {
    getModuleManager(project).map(_.getModules).getOrElse(Array())
  }
}
