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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal._
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.external.repl.StackRepl._
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.settings.HaskellSettingsState
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
    getReplsManager(project).flatMap(_.setAndGetProjectRepl(stackComponentInfo))
  }

  def getGlobalRepl(project: Project): Option[GlobalStackRepl] = {
    getReplsManager(project).map(_.getGlobalRepl)
  }

  private def createCabalInfos(project: Project): Iterable[CabalInfo] = {
    val modules = HaskellProjectUtil.findProjectModules(project)
    val moduleDirectoryPaths = modules.map(HaskellProjectUtil.getModulePath)
    moduleDirectoryPaths.flatMap(p => HaskellProjectUtil.findCabalFile(p).flatMap(cf => CabalInfo.create(project, cf)))
  }

  private def createStackComponentInfo(project: Project, cabalInfos: Iterable[CabalInfo]): Iterable[StackComponentInfo] = {
    cabalInfos.flatMap(_.cabalStanzas).map {
      case cs: LibraryCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, LibType, cs.sourceDirs)
      case cs: ExecutableCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, ExeType, cs.sourceDirs)
      case cs: TestSuiteCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, TestSuiteType, cs.sourceDirs)
      case cs: BenchmarkCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, BenchmarkType, cs.sourceDirs)
    }
  }

  case class StackComponentInfo(packageName: String, target: String, stanzaType: StanzaType, sourceDirs: Seq[String])

}

private[external] class StackReplsManager(val project: Project) {

  private val globalRepl: GlobalStackRepl = new GlobalStackRepl(project, HaskellSettingsState.getReplTimeout)

  @volatile
  private var projectLibraryRepl: Option[ProjectStackRepl] = None

  @volatile
  private var projectNonLibraryRepl: Option[ProjectStackRepl] = None

  val cabalInfos: Iterable[CabalInfo] = StackReplsManager.createCabalInfos(project)
  val stackComponentInfos: Iterable[StackComponentInfo] = StackReplsManager.createStackComponentInfo(project, cabalInfos)

  private val ignoredHaskellFiles = Seq("setup.hs", "hlint.hs")

  def restartProjectRepl(repl: ProjectStackRepl): Unit = synchronized {
    if (repl.available && !repl.starting) {
      repl.exit()
      repl.clearLoadedInfo()
      repl.start()
    }
  }

  def getProjectLibraryRepl: Option[ProjectStackRepl] = projectLibraryRepl

  def getProjectNonLibraryRepl: Option[ProjectStackRepl] = projectNonLibraryRepl

  def getGlobalRepl: GlobalStackRepl = globalRepl

  private def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (ignoredHaskellFiles.contains(psiFile.getName.toLowerCase) && HaskellProjectUtil.findModule(psiFile).exists(m => HaskellFileUtil.getAbsoluteFilePath(psiFile) == HaskellProjectUtil.getModulePath(m).getAbsolutePath)) {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"${psiFile.getName} can not be loaded in REPL")
      None
    } else {
      if (StackProjectManager.isBuilding(project)) {
        HaskellEditorUtil.showStatusBarInfoMessage(project, "Haskell support is not available while (re)building project")
        None
      } else {
        val componentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
        componentInfo match {
          case Some(ci) => setAndGetProjectRepl(ci)
          case None =>
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not switch to project Stack REPL for file ${psiFile.getName} because no Stack target build info")
            None
        }
      }
    }
  }

  private def setAndGetProjectRepl(componentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    if (componentInfo.stanzaType == LibType) {
      synchronized {
        projectLibraryRepl = Some(getProjectRepl(projectLibraryRepl, componentInfo))
        projectLibraryRepl
      }
    } else {
      synchronized {
        projectNonLibraryRepl = Some(getProjectRepl(projectNonLibraryRepl, componentInfo))
        projectNonLibraryRepl
      }
    }
  }

  private def getProjectRepl(projectStackRepl: Option[ProjectStackRepl], componentInfo: StackComponentInfo): ProjectStackRepl = {
    projectStackRepl match {
      case None =>
        createAndStartProjectRepl(componentInfo)
      case Some(repl) =>
        if (repl.target.contains(componentInfo.target)) {
          repl
        } else {
          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
            override def run(): Unit = repl.exit()
          })
          createAndStartProjectRepl(componentInfo)
        }
    }
  }

  private def createAndStartProjectRepl(componentInfo: StackComponentInfo) = {
    val repl = new ProjectStackRepl(project, componentInfo, HaskellSettingsState.getReplTimeout)
    repl.start()
    repl
  }
}