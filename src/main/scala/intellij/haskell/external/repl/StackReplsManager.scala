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

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal._
import intellij.haskell.external.component._
import intellij.haskell.external.repl.StackRepl._
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil, HaskellProjectUtil, ScalaUtil}

private[external] object StackReplsManager {

  def getReplsManager(project: Project): Option[StackReplsManager] = {
    StackProjectManager.getStackProjectManager(project).flatMap(_.getStackReplsManager)
  }

  def getRunningProjectRepls(project: Project): Iterable[ProjectStackRepl] = {
    getReplsManager(project).map(_.getRunningProjectRepls).getOrElse(Iterable())
  }

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    getReplsManager(psiFile.getProject).flatMap(_.findProjectRepl(psiFile))
  }

  def getRunningProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    HaskellComponentsManager.findStackComponentInfo(psiFile).flatMap(ci => getRunningProjectRepl(psiFile.getProject, ci))
  }

  def getRunningProjectRepl(project: Project, stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getRunningProjectRepl(stackComponentInfo))
  }

  def getGlobalRepl(project: Project): Option[GlobalStackRepl] = {
    getReplsManager(project).map(_.getGlobalRepl)
  }

  private def createCabalInfos(project: Project): Iterable[CabalInfo] = {
    val modules = HaskellProjectUtil.findProjectModules(project)
    val moduleDirs = modules.map(HaskellProjectUtil.getModuleDir)
    moduleDirs.flatMap(p => HaskellProjectUtil.findCabalFile(p).flatMap(cf => CabalInfo.create(project, cf)))
  }

  private def createStackComponentInfo(project: Project, cabalInfos: Iterable[CabalInfo]): Iterable[StackComponentInfo] = {
    cabalInfos.flatMap(_.cabalStanzas).map {
      case cs: LibraryCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, LibType, cs.sourceDirs, None)
      case cs: ExecutableCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, ExeType, cs.sourceDirs, cs.mainIs)
      case cs: TestSuiteCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, TestSuiteType, cs.sourceDirs, cs.mainIs)
      case cs: BenchmarkCabalStanza => StackComponentInfo(cs.packageName, cs.targetName, BenchmarkType, cs.sourceDirs, cs.mainIs)
    }
  }

  case class StackComponentInfo(packageName: String, target: String, stanzaType: StanzaType, sourceDirs: Seq[String], mainIs: Option[String])

}

private[external] class StackReplsManager(val project: Project) {

  import scala.collection.JavaConverters._

  private val globalRepl: GlobalStackRepl = new GlobalStackRepl(project, HaskellSettingsState.getReplTimeout)

  private val projectRepls = new ConcurrentHashMap[StackComponentInfo, ProjectStackRepl]().asScala

  val cabalInfos: Iterable[CabalInfo] = StackReplsManager.createCabalInfos(project)

  val stackComponentInfos: Iterable[StackComponentInfo] = StackReplsManager.createStackComponentInfo(project, cabalInfos)

  private final val IgnoredHaskellFiles = Seq("setup.hs", "hlint.hs")

  def getRunningProjectRepl(stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    projectRepls.get(stackComponentInfo)
  }

  def getRunningProjectRepls: Iterable[ProjectStackRepl] = {
    projectRepls.values
  }

  def getGlobalRepl: GlobalStackRepl = globalRepl

  private def findProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (IgnoredHaskellFiles.contains(psiFile.getName.toLowerCase) &&
      HaskellProjectUtil.findModule(psiFile).exists(m => findContainingDirectory(psiFile).exists(vf => HaskellFileUtil.getAbsolutePath(vf) == HaskellProjectUtil.getModuleDir(m).getPath))) {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"${psiFile.getName} can not be loaded in REPL")
      None
    } else {
      if (StackProjectManager.isBuilding(project)) {
        HaskellEditorUtil.showStatusBarInfoMessage(project, "Haskell support is not available while (re)building project")
        None
      } else {
        val componentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
        componentInfo match {
          case Some(ci) => Some(getProjectRepl(ci, psiFile))
          case None =>
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not switch to project Stack REPL for file ${psiFile.getName} because no Stack target could be found for this file")
            None
        }
      }
    }
  }

  private def findContainingDirectory(psiFile: PsiFile): Option[VirtualFile] = {
    ApplicationManager.getApplication.runReadAction(new Computable[Option[VirtualFile]] {
      override def compute(): Option[VirtualFile] = {
        Option(psiFile.getContainingDirectory).map(_.getVirtualFile)
      }
    })
  }

  private def getProjectRepl(componentInfo: StackComponentInfo, psiFile: PsiFile): ProjectStackRepl = synchronized {
    projectRepls.get(componentInfo) match {
      case Some(r) => r
      case None =>
        val repl = createAndStartProjectRepl(componentInfo)
        projectRepls.put(componentInfo, repl)

        // Already load global info in cache here to prevent a file has to be loaded twice because library modules are obtained in REPL without any module loaded.
        ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
          HaskellComponentsManager.findStackComponentGlobalInfo(psiFile)
        })

        repl
    }
  }

  private def createAndStartProjectRepl(componentInfo: StackComponentInfo): ProjectStackRepl = {
    val repl = new ProjectStackRepl(project, componentInfo, HaskellSettingsState.getReplTimeout)
    repl.start()
    repl
  }
}