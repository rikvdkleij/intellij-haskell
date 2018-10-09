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
import com.intellij.openapi.roots.{ModuleRootManager, ProjectRootManager, TestSourcesFilter}
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.component.{HaskellComponentsManager, NoInfo}
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

object HaskellProjectUtil {

  final val Prelude = "Prelude"

  def setNoDiagnosticsShowCaretFlag(project: Project): Boolean = {
    HaskellComponentsManager.getGhcVersion(project).exists(ghcVersion =>
      ghcVersion >= GhcVersion(8, 2, 1)
    )
  }

  def isValidHaskellProject(project: Project, notifyNoSdk: Boolean): Boolean = {
    val haskellModules = HaskellProjectUtil.findProjectHaskellModules(project)
    val stackPathsDefined = haskellModules.map(m => HaskellSdkType.getStackPath(project, notifyNoSdk)).forall(_.isDefined)
    isHaskellProject(project) && stackPathsDefined
  }

  def isHaskellProject(project: Project): Boolean = {
    findProjectHaskellModules(project).nonEmpty
  }

  def findFile(filePath: String, project: Project): (Option[VirtualFile], Either[NoInfo, Option[PsiFile]]) = {
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

  // TODO Refactor to ADT: projectFile, libraryFile, otherFile

  // File can both project and library file in multi package projects
  // Being project file is leading
  def isLibraryFile(psiFile: PsiFile): Boolean = {
    if (psiFile.getVirtualFile == null) {
      false
    } else {
      !isProjectFile(psiFile) && !isIgnoredFile(psiFile.getProject, psiFile.getVirtualFile) && !isGeneratedFile(psiFile.getProject, psiFile.getVirtualFile)
    }
  }

  /**
    * findVirtualFile returns null when file is only in memory so then is must be a project file
    */
  def isProjectFile(psiFile: PsiFile): Boolean = {
    HaskellFileUtil.findVirtualFile(psiFile).forall(vf => isProjectFile(psiFile.getProject, vf))
  }

  def isProjectFile(project: Project, virtualFile: VirtualFile): Boolean = {
    !isIgnoredFile(project, virtualFile) && !isGeneratedFile(project, virtualFile) && findProjectHaskellModules(project).exists(m => FileUtil.isAncestor(Paths.get(getModuleDir(m).getAbsolutePath).toFile, Paths.get(virtualFile.getPath).toFile, true))
  }

  private final val IgnoredHaskellFiles = Seq("setup.hs", "hlint.hs")

  private def isIgnoredFile(project: Project, virtualFile: VirtualFile): Boolean = {
    if (IgnoredHaskellFiles.contains(virtualFile.getName.toLowerCase) &&
      HaskellProjectUtil.findModuleForVirtualFile(project, virtualFile).exists(m => findContainingDirectory(virtualFile).exists(vf => HaskellFileUtil.getAbsolutePath(vf) == HaskellProjectUtil.getModuleDir(m).getPath))) {
      HaskellNotificationGroup.logInfoEvent(project, s"`${virtualFile.getName}` is ignored")
      true
    } else {
      false
    }
  }

  private def isGeneratedFile(project: Project, virtualFile: VirtualFile) = {
    virtualFile.getName.startsWith("Paths_") && FileUtil.isAncestor(getStackWorkDirectory(project).toFile, Paths.get(virtualFile.getPath).toFile, true)
  }

  private def getStackWorkDirectory(project: Project) = {
    Paths.get(project.getBasePath, GlobalInfo.StackWorkDirName)
  }

  private def findContainingDirectory(virtualFile: VirtualFile): Option[VirtualFile] = {
    Option(virtualFile.getParent)
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
    val modules = findProjectHaskellModules(project)
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
    val modules = findProjectHaskellModules(project)
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
    val projectModules = findProjectHaskellModules(project).map(GlobalSearchScope.moduleScope)
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
    project.isDisposed.optionNot(Option(ProjectRootManager.getInstance(project))).flatten
  }

  def getModuleManager(project: Project): Option[ModuleManager] = {
    project.isDisposed.optionNot(Option(ModuleManager.getInstance(project))).flatten
  }

  def getModuleRootManager(project: Project, module: Module): Option[ModuleRootManager] = {
    project.isDisposed.optionNot(Option(ModuleRootManager.getInstance(module))).flatten
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

  def findProjectHaskellModules(project: Project): Iterable[Module] = {
    ModuleManager.getInstance(project).getModules.filter(_.getModuleTypeName == HaskellModuleType.Id)
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
