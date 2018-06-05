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

import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtil, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile, PsiManager}
import intellij.haskell.HaskellFile
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.module.HaskellModuleType
import intellij.haskell.sdk.HaskellSdkType

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

  def findFile(filePath: String, project: Project): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    file.flatMap(f => Option(PsiManager.getInstance(project).findFile(f)).flatMap {
      case f: HaskellFile => Some(f)
      case _ => None
    })
  }

  // File can both project and library file in multi package projects
  // Being project file is leading
  def isLibraryFile(psiFile: PsiFile): Boolean = {
    !isProjectFile(psiFile)
  }

  def isLibraryFile(virtualFile: VirtualFile, project: Project): Boolean = {
    !isProjectFile(virtualFile, project)
  }

  def isProjectTestFile(psiFile: PsiFile): Boolean = {
    HaskellFileUtil.findVirtualFile(psiFile).forall(vf => isProjectTestFile(vf, psiFile.getProject))
  }

  def isProjectTestFile(virtualFile: VirtualFile, project: Project): Boolean = {
    if (project.isDisposed) {
      false
    } else {
      getProjectRootManager(project).getFileIndex.isInTestSourceContent(virtualFile)
    }
  }

  /**
    * findVirtualFile returns null when file is only in memory so then is must be a project file
    */
  def isProjectFile(psiFile: PsiFile): Boolean = {
    HaskellFileUtil.findVirtualFile(psiFile).forall(vf => isProjectFile(vf, psiFile.getProject))
  }

  def isProjectFile(virtualFile: VirtualFile, project: Project): Boolean = {
    if (project.isDisposed) {
      false
    } else {
      getProjectRootManager(project).getFileIndex.isContentSourceFile(virtualFile)
    }
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

  def findStackFile(project: Project): Option[File] = {
    findStackFile(new File(project.getBasePath))
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

  def getProjectRootManager(project: Project): ProjectRootManager = {
    ProjectRootManager.getInstance(project)
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

  import scala.collection.JavaConverters._

  def findProjectModules(project: Project): Iterable[Module] = {
    ModuleUtil.getModulesOfType(project, HaskellModuleType.getInstance).asScala
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
