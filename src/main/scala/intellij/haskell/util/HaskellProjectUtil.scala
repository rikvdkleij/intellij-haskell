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

import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModuleRootManager, ProjectRootManager, TestSourcesFilter}
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.{HaskellComponentsManager, NoInfo, NoInfoAvailable}
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType

object HaskellProjectUtil {

  final val Prelude = "Prelude"

  def setNoDiagnosticsShowCaretFlag(project: Project): Boolean = {
    HaskellComponentsManager.getGhcVersion(project).exists(ghcVersion =>
      ghcVersion >= GhcVersion(8, 2, 1)
    )
  }

  def isValidHaskellProject(project: Project, notifyNoSdk: Boolean): Boolean = {
    isHaskellProject(project) && HaskellSdkType.getStackBinaryPath(project, notifyNoSdk).isDefined
  }

  def isHaskellProject(project: Project): Boolean = {
    findProjectHaskellModules(project).nonEmpty
  }

  def findFile(filePath: String, project: Project): (Option[VirtualFile], Either[NoInfo, PsiFile]) = {
    val virtualFile = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    val psiFile = virtualFile.map(f => HaskellFileUtil.convertToHaskellFileInReadAction(project, f)) match {
      case Some(r) => r
      case None => Left(NoInfoAvailable(filePath, "-"))
    }
    (virtualFile, psiFile)
  }

  def findFile2(filePath: String, project: Project): (Option[VirtualFile], Option[PsiFile]) = {
    val virtualFile = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    val psiFile = virtualFile.map(f => HaskellFileUtil.convertToHaskellFileDispatchThread(project, f)) match {
      case Some(r) => r
      case None => None
    }
    (virtualFile, psiFile)
  }

  def findVirtualFile(filePath: String, project: Project): Option[VirtualFile] = {
    Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
  }

  trait HaskellProjectFileType

  case object SourceFile extends HaskellProjectFileType

  case object LibraryFile extends HaskellProjectFileType

  case object Other extends HaskellProjectFileType

  def getHaskellProjectFileType(psiFile: PsiFile): Option[HaskellProjectFileType] = {
    HaskellFileUtil.findVirtualFile(psiFile) match {
      case None => None
      case Some(vf) =>
        val project = psiFile.getProject
        Some(getHaskellProjectFileType(project, vf))
    }
  }

  def getHaskellProjectFileType(project: Project, virtualFile: VirtualFile): HaskellProjectFileType = {
    if (isConfigFile(project, virtualFile)) {
      HaskellNotificationGroup.logInfoEvent(project, s"`${virtualFile.getName}` is ignored")
      Other
    } else if (isModuleFile(project, virtualFile)) {
      SourceFile
    } else {
      LibraryFile
    }
  }

  def isSourceFile(project: Project, virtualFile: VirtualFile): Boolean = {
    getHaskellProjectFileType(project, virtualFile) == SourceFile
  }

  def isSourceFile(psiFile: PsiFile): Boolean = {
    getHaskellProjectFileType(psiFile).contains(SourceFile)
  }

  def isLibraryFile(psiFile: PsiFile): Boolean = {
    getHaskellProjectFileType(psiFile).contains(LibraryFile)
  }

  private def isModuleFile(project: Project, virtualFile: VirtualFile): Boolean = {
    findProjectHaskellModules(project).exists(m => FileUtil.isAncestor(getModuleDir(m), new File(virtualFile.getPath), true))
  }

  private final val ConfigHaskellFiles = Seq("setup.hs", "hlint.hs")

  private def isConfigFile(project: Project, virtualFile: VirtualFile): Boolean = {
    ConfigHaskellFiles.contains(virtualFile.getName.toLowerCase) &&
      HaskellProjectUtil.findModuleForVirtualFile(project, virtualFile).exists(m => findContainingDirectory(virtualFile).exists(vf => HaskellFileUtil.getAbsolutePath(vf) == HaskellProjectUtil.getModuleDir(m).getPath))
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

  def getProjectAndLibrariesModulesSearchScope(project: Project): GlobalSearchScope = {
    val projectModules = findProjectHaskellModules(project).map(m => GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(m, true))
    if (projectModules.isEmpty) {
      GlobalSearchScope.EMPTY_SCOPE
    } else {
      projectModules.reduce(_.uniteWith(_))
    }
  }

  def getSearchScope(project: Project, includeNonProjectItems: Boolean): GlobalSearchScope = {
    if (includeNonProjectItems) getProjectAndLibrariesModulesSearchScope(project) else GlobalSearchScope.projectScope(project)
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

  def findProjectPackageNames(project: Project): Seq[String] = {
    HaskellComponentsManager.findProjectModulePackageNames(project).map(_._2)
  }
}

case class GhcVersion(major: Int, minor: Int, patch: Int) extends Ordered[GhcVersion] {
  def compare(that: GhcVersion): Int = GhcVersion.asc.compare(this, that)

  def prettyString: String = {
    s"$major.$minor.$patch"
  }
}

object GhcVersion {
  val asc: Ordering[GhcVersion] = Ordering.by(unapply)

  def parse(version: String): GhcVersion = {
    val parts = version.split('.')
    GhcVersion(parts(0).toInt, parts(1).toInt, parts(2).toInt)
  }
}
