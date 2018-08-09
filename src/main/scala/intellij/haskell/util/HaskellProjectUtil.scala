/*
 * Copyright 2014-2018 Rik van der Kleij
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
import java.nio.file.Paths

import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ProjectRootManager, TestSourcesFilter}
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.util.ApplicationUtil.ReadActionTimeout

object HaskellProjectUtil {

  final val Prelude = "Prelude"

  def setNoDiagnosticsShowCaretFlag(project: Project): Boolean = {
    HaskellProjectUtil.getGhcVersion(project).exists(ghcVersion =>
      ghcVersion >= GhcVersion(8, 2, 1)
    )
  }

  def isValidHaskellProject(project: Project, notifyNoSdk: Boolean): Boolean = {
    val stackPath = HaskellSdkType.getStackPath(project, notifyNoSdk)
    isHaskellProject(project) && stackPath.isDefined
  }

  def isHaskellProject(project: Project): Boolean = {
    findProjectModules(project).nonEmpty
  }

  def findFile(filePath: String, project: Project): (Option[VirtualFile], Either[ReadActionTimeout, Option[PsiFile]]) = {
    val virtualFile = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    val psiFile = virtualFile.map(f => HaskellFileUtil.convertToHaskellFileInReadAction(project, f)) match {
      case Some(r) => r
      case None => Right(None)
    }
    (virtualFile, psiFile)
  }

  def findVirtualFile(filePath: String, project: Project): Option[VirtualFile] = {
    Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
  }

  // File can both project and library file in multi package projects
  // Being project file is leading
  def isLibraryFile(psiFile: PsiFile): Boolean = {
    !isProjectFile(psiFile)
  }

  /**
    * findVirtualFile returns null when file is only in memory so then is must be a project file
    */
  def isProjectFile(psiFile: PsiFile): Boolean = {
    HaskellFileUtil.findVirtualFile(psiFile).forall(vf => isProjectFile(vf, psiFile.getProject))
  }

  def isProjectFile(virtualFile: VirtualFile, project: Project): Boolean = {
    FileUtil.isAncestor(Paths.get(project.getBasePath).toFile, Paths.get(virtualFile.getPath).toFile, true)
  }

  def isProjectTestFile(psiFile: PsiFile): Option[Boolean] = {
    Option(psiFile.getOriginalFile.getVirtualFile).map(vf => isProjectTestFile(vf, psiFile.getProject))
  }

  def isProjectTestFile(virtualFile: VirtualFile, project: Project): Boolean = {
    TestSourcesFilter.isTestSources(virtualFile, project)
  }

  def getModuleDir(module: Module): File = {
    new File(ModuleUtilCore.getModuleDirPath(module))
  }

  def findCabalFiles(project: Project): Iterable[File] = {
    val modules = findProjectModules(project)
    val dirs = modules.map(getModuleDir)
    dirs.flatMap(findCabalFile)
  }

  def findCabalFile(directory: File): Option[File] = {
    directory.listFiles.find(_.getName.endsWith(".cabal"))
  }

  def findStackFile(directory: File): Option[File] = {
    directory.listFiles.find(_.getName == "stack.yaml")
  }

  def findPackageFiles(project: Project): Iterable[File] = {
    val modules = findProjectModules(project)
    val dirs = modules.map(getModuleDir)
    dirs.flatMap(findCabalFile)
    dirs.flatMap(findPackageFile)
  }

  def findStackFile(project: Project): Option[File] = {
    findStackFile(new File(project.getBasePath))
  }

  def findPackageFile(directory: File): Option[File] = {
    directory.listFiles.find(_.getName == "package.yaml")
  }

  def getProjectModulesSearchScope(project: Project): GlobalSearchScope = {
    val projectModules = findProjectModules(project).map(GlobalSearchScope.moduleScope)
    if (projectModules.isEmpty) {
      GlobalSearchScope.EMPTY_SCOPE
    } else {
      projectModules.reduce(_.uniteWith(_))
    }
  }

  def getSearchScope(project: Project, includeNonProjectItems: Boolean): GlobalSearchScope = {
    if (includeNonProjectItems) GlobalSearchScope.allScope(project) else HaskellProjectUtil.getProjectModulesSearchScope(project)
  }

  import ScalaUtil._

  def getProjectRootManager(project: Project): Option[ProjectRootManager] = {
    if (project.isDisposed) {
      None
    } else {
      Option(ProjectRootManager.getInstance(project))
    }
  }

  def getModuleManager(project: Project): Option[ModuleManager] = {
    project.isDisposed.optionNot(ModuleManager.getInstance(project))
  }

  def findModule(psiElement: PsiElement): Option[Module] = {
    Option(ModuleUtilCore.findModuleForPsiElement(psiElement))
  }

  def findModuleForFile(psiFile: PsiFile): Option[Module] = {
    Option(ModuleUtilCore.findModuleForFile(psiFile))
  }

  def findModuleForVirtualFile(project: Project, virtualFile: VirtualFile): Option[Module] = {
    Option(ModuleUtilCore.findModuleForFile(virtualFile, project))
  }

  def findProjectModules(project: Project): Iterable[Module] = {
    ModuleManager.getInstance(project).getModules.filter(_.getModuleTypeName == HaskellModuleType.Id)
  }

  def getGhcVersion(project: Project): Option[GhcVersion] = {
    StackCommandLine.run(project, Seq("exec", "--", "ghc", "--numeric-version"))
      .map(o => GhcVersion.parse(o.getStdout.trim))
  }

}

case class GhcVersion(major: Int, minor: Int, patch: Int) extends Ordered[GhcVersion] {
  def compare(that: GhcVersion): Int = GhcVersion.asc.compare(this, that)
}

object GhcVersion {
  val asc: Ordering[GhcVersion] = Ordering.by(unapply)

  def parse(version: String): GhcVersion = {
    val parts = version.split('.')
    GhcVersion(parts(0).toInt, parts(1).toInt, parts(2).toInt)
  }
}
