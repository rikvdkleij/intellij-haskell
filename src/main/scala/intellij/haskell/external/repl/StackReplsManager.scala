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

package intellij.haskell.external.repl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.annotator.HaskellAnnotator.getDaemonCodeAnalyzer
import intellij.haskell.cabal._
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil, HaskellProjectUtil}

private[external] object StackReplsManager {

  def getReplsManager(project: Project): Option[StackReplsManager] = {
    StackProjectManager.getStackProjectManager(project).map(_.getStackReplsManager)
  }

  def getProjectLibraryRepl(project: Project): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectLibraryRepl)
  }

  def getProjectNonLibraryRepl(project: Project): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectNonLibraryRepl)
  }

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    getReplsManager(psiFile.getProject).flatMap(_.getProjectRepl(psiFile))
  }

  def getProjectRepl(project: Project, stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectRepl(stackComponentInfo, None))
  }

  def getGlobalRepl(project: Project): Option[GlobalStackRepl] = {
    getReplsManager(project).map(_.getGlobalRepl)
  }

  def createCabalInfos(project: Project): Iterable[CabalInfo] = {
    val modules = HaskellProjectUtil.findProjectModules(project)
    val moduleDirectoryPaths = modules.map(HaskellProjectUtil.getModulePath)
    moduleDirectoryPaths.flatMap(p => HaskellProjectUtil.findCabalFile(p).flatMap(cf => CabalInfo.create(project, cf)))
  }

  def createStackComponentInfo(project: Project, cabalInfos: Iterable[CabalInfo]): Iterable[StackComponentInfo] = {
    cabalInfos.flatMap(_.getCabalStanzas).map {
      case cs: LibraryCabalStanza => StackComponentInfo(cs.packageName, cs.getTargetName, LibType, cs.getSourceDirs)
      case cs: ExecutableCabalStanza => StackComponentInfo(cs.packageName, cs.getTargetName, ExeType, cs.getSourceDirs)
      case cs: TestSuiteCabalStanza => StackComponentInfo(cs.packageName, cs.getTargetName, TestSuiteType, cs.getSourceDirs)
      case cs: BenchmarkCabalStanza => StackComponentInfo(cs.packageName, cs.getTargetName, BenchmarkType, cs.getSourceDirs)
    }
  }

  case class StackComponentInfo(packageName: String, target: String, stanzaType: StanzaType, sourceDirs: Seq[String])

}

private[external] class StackReplsManager(val project: Project,
                                          private[this] val globalRepl: GlobalStackRepl,
                                          private[this] var projectLibraryRepl: Option[ProjectStackRepl],
                                          private[this] var projectNonLibraryRepl: Option[ProjectStackRepl]) {

  val cabalInfos: Iterable[CabalInfo] = StackReplsManager.createCabalInfos(project)
  val stackComponentInfos: Iterable[StackComponentInfo] = StackReplsManager.createStackComponentInfo(project, cabalInfos)

  def restartProjectNonLibraryRepl(): Unit = synchronized {
    projectNonLibraryRepl match {
      case Some(repl) =>
        repl.exit()
        repl.clearLoadedInfo()
        repl.start()
      case None => ()
    }
  }

  def getProjectLibraryRepl: Option[ProjectStackRepl] = projectLibraryRepl

  def getProjectNonLibraryRepl: Option[ProjectStackRepl] = projectNonLibraryRepl

  def getGlobalRepl: GlobalStackRepl = globalRepl

  private val ignoredHaskellFiles = Seq("setup.hs", "hlint.hs")

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (ignoredHaskellFiles.contains(psiFile.getName.toLowerCase) && HaskellProjectUtil.getModule(psiFile).exists(m => HaskellFileUtil.getAbsoluteFilePath(psiFile) == HaskellProjectUtil.getModulePath(m).getAbsolutePath)) {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"${psiFile.getName} can not be loaded in REPL")
      None
    } else {
      if (StackProjectManager.isBuilding(project)) {
        HaskellEditorUtil.showStatusBarNotificationBalloon(project, "Haskell support is not available while (re)building project")
        None
      } else {
        val componentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
        componentInfo match {
          case Some(ci) => getProjectRepl(ci, Some(psiFile))
          case None =>
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not switch to project Stack repl for file ${psiFile.getName} because no Stack target build info")
            None
        }
      }
    }
  }

  def getProjectRepl(componentInfo: StackComponentInfo, psiFile: Option[PsiFile]): Option[ProjectStackRepl] = {
    if (componentInfo.stanzaType == LibType) {
      getProjectLibraryRepl(componentInfo, psiFile)
    } else {
      getProjectNonLibraryRepl(componentInfo, psiFile)
    }
  }

  private def getProjectLibraryRepl(componentInfo: StackComponentInfo, psiFile: Option[PsiFile]): Option[ProjectStackRepl] = synchronized {
    projectLibraryRepl match {
      case None =>
        projectLibraryRepl = Some(new ProjectStackRepl(project, componentInfo.stanzaType, componentInfo.target, componentInfo.sourceDirs))
        projectLibraryRepl.foreach(_.start())
        psiFile.foreach(pf => getDaemonCodeAnalyzer(pf.getProject).getFileStatusMap.dispose())
        psiFile.foreach(HaskellAnnotator.restartDaemonCodeAnalyzerForFile)
        projectLibraryRepl
      case Some(repl) =>
        if (repl.target.contains(componentInfo.target)) {
          projectLibraryRepl
        } else {
          repl.exit()
          repl.clearLoadedInfo()
          repl.target = Some(componentInfo.target)
          repl.sourceDirs = componentInfo.sourceDirs
          repl.stanzaType = Some(componentInfo.stanzaType)
          repl.start()
          Some(repl)
        }
    }
  }

  private def getProjectNonLibraryRepl(componentInfo: StackComponentInfo, psiFile: Option[PsiFile]): Option[ProjectStackRepl] = synchronized {
    projectNonLibraryRepl match {
      case None =>
        projectNonLibraryRepl = Some(new ProjectStackRepl(project, componentInfo.stanzaType, componentInfo.target, componentInfo.sourceDirs))
        projectNonLibraryRepl.foreach(_.start())
        psiFile.foreach(pf => getDaemonCodeAnalyzer(pf.getProject).getFileStatusMap.dispose())
        psiFile.foreach(HaskellAnnotator.restartDaemonCodeAnalyzerForFile)
        projectNonLibraryRepl
      case Some(repl) =>
        if (repl.target.contains(componentInfo.target)) {
          projectNonLibraryRepl
        } else {
          repl.exit()
          repl.clearLoadedInfo()
          repl.target = Some(componentInfo.target)
          repl.sourceDirs = componentInfo.sourceDirs
          repl.stanzaType = Some(componentInfo.stanzaType)
          repl.start()
          Some(repl)
        }
    }
  }
}