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

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal._
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil}

private[external] object StackReplsManager {

  def getReplsManager(project: Project): Option[StackReplsManager] = {
    StackProjectManager.getStackProjectManager(project).map(_.getStackReplsManager)
  }

  def getProjectRepl(project: Project): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectRepl)
  }

  def getProjectTestRepl(project: Project): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectTestRepl)
  }

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    getReplsManager(psiFile.getProject).flatMap(_.getProjectRepl(psiFile))
  }

  def getProjectRepl(project: Project, stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getProjectRepl(stackComponentInfo))
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
                                          private[this] var projectRepl: Option[ProjectStackRepl],
                                          private[this] var projectTestRepl: Option[ProjectStackRepl]) {

  val cabalInfos: Iterable[CabalInfo] = StackReplsManager.createCabalInfos(project)
  val stackComponentInfos: Iterable[StackComponentInfo] = StackReplsManager.createStackComponentInfo(project, cabalInfos)

  def restartProjectTestRepl(): Unit = synchronized {
    projectTestRepl match {
      case Some(repl) =>
        repl.exit()
        repl.clearLoadedInfo()
        repl.start()
      case None => ()
    }
  }

  def getProjectRepl: Option[ProjectStackRepl] = projectRepl

  def getProjectTestRepl: Option[ProjectStackRepl] = projectTestRepl

  def getGlobalRepl: GlobalStackRepl = globalRepl

  private val ignoredHaskellFiles = Seq("setup.hs", "hlint.hs")

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (ignoredHaskellFiles.contains(psiFile.getName.toLowerCase) && psiFile.getVirtualFile.getParent.getPath == HaskellProjectUtil.getModulePath(ModuleUtilCore.findModuleForPsiElement(psiFile)).getAbsolutePath) {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"${psiFile.getName} can not be loaded in REPL")
      None
    } else {
      if (StackProjectManager.isBuilding(project)) {
        HaskellEditorUtil.showStatusBarNotificationBalloon(project, "Haskell support is not available while (re)building project")
        None
      } else {
        val componentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
        componentInfo match {
          case Some(ci) => getProjectRepl(ci)
          case None =>
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not switch to project Stack repl for file ${psiFile.getName} because no Stack target build info")
            None
        }
      }
    }
  }

  def getProjectRepl(componentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    if (componentInfo.stanzaType == LibType || componentInfo.stanzaType == ExeType) {
      getProjectProductionRepl(componentInfo)
    } else {
      getProjectTestRepl(componentInfo)
    }
  }

  private def getProjectProductionRepl(componentInfo: StackComponentInfo): Option[ProjectStackRepl] = synchronized {
    projectRepl match {
      case None =>
        projectRepl = Some(new ProjectStackRepl(project, componentInfo.stanzaType, componentInfo.target, componentInfo.sourceDirs))
        projectRepl.foreach(_.start())
        projectRepl
      case Some(repl) =>
        if (repl.target.contains(componentInfo.target)) {
          projectRepl
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

  private def getProjectTestRepl(componentInfo: StackComponentInfo): Option[ProjectStackRepl] = synchronized {
    projectTestRepl match {
      case None =>
        projectTestRepl = Some(new ProjectStackRepl(project, componentInfo.stanzaType, componentInfo.target, componentInfo.sourceDirs))
        projectTestRepl.foreach(_.start())
        projectTestRepl
      case Some(repl) =>
        if (repl.target.contains(componentInfo.target)) {
          projectTestRepl
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