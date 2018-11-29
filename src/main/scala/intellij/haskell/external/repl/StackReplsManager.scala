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

package intellij.haskell.external.repl

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal._
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.component._
import intellij.haskell.external.repl.StackRepl._
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._

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

  def getProjectRepl(project: Project, stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    getReplsManager(project).map(_.getProjectRepl(stackComponentInfo))
  }

  def getRunningProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    HaskellComponentsManager.findStackComponentInfo(psiFile).flatMap(ci => getRunningProjectRepl(psiFile.getProject, ci))
  }

  def getRunningProjectRepl(project: Project, stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    getReplsManager(project).flatMap(_.getRunningProjectRepl(stackComponentInfo))
  }

  def getGlobalRepl(project: Project): Option[GlobalStackRepl] = {
    val repl = getReplsManager(project).map(_.getGlobalRepl)
    repl.foreach(r => if (!r.available && !r.starting) r.start())
    repl
  }

  private def createCabalInfos(project: Project): Iterable[(Module, CabalInfo)] = {
    val modules = HaskellProjectUtil.findProjectHaskellModules(project)
    val moduleDirs = modules.map(HaskellProjectUtil.getModuleDir)
    if (moduleDirs.isEmpty) {
      HaskellNotificationGroup.logWarningBalloonEvent(project, s"No Haskell modules found for project `${project.getName}`. Check your project configuration.")
      Iterable()
    } else {
      val cabalFiles = for {
        m <- modules
        dir = HaskellProjectUtil.getModuleDir(m)
        cf <- HaskellProjectUtil.findCabalFile(dir)
        ci <- CabalInfo.create(project, cf)
      } yield (m, ci)
      if (cabalFiles.isEmpty) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, s"No Cabal files found for project `${project.getName}`. Check your project configuration.")
      }
      cabalFiles
    }
  }

  private def createStackComponentInfos(project: Project, moduleCabalInfos: Iterable[(Module, CabalInfo)]): Iterable[StackComponentInfo] = {
    moduleCabalInfos.flatMap {
      case (m: Module, cabalInfo: CabalInfo) => cabalInfo.cabalStanzas.map {
        case cs: LibraryCabalStanza => StackComponentInfo(m, cs.packageName, cs.targetName, LibType, cs.sourceDirs, None, cs.isNoImplicitPreludeActive, cs.buildDepends, cs.exposedModuleNames)
        case cs: ExecutableCabalStanza => StackComponentInfo(m, cs.packageName, cs.targetName, ExeType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
        case cs: TestSuiteCabalStanza => StackComponentInfo(m, cs.packageName, cs.targetName, TestSuiteType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
        case cs: BenchmarkCabalStanza => StackComponentInfo(m, cs.packageName, cs.targetName, BenchmarkType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
      }
    }
  }
}

private[external] class StackReplsManager(val project: Project) {

  import scala.collection.JavaConverters._

  private val globalRepl: GlobalStackRepl = GlobalStackRepl(project, HaskellSettingsState.getReplTimeout)

  private val projectRepls = new ConcurrentHashMap[StackComponentInfo, ProjectStackRepl]().asScala

  val moduleCabalInfos: Iterable[(Module, CabalInfo)] = StackReplsManager.createCabalInfos(project)

  val stackComponentInfos: Iterable[StackComponentInfo] = StackReplsManager.createStackComponentInfos(project, moduleCabalInfos)

  def getRunningProjectRepl(stackComponentInfo: StackComponentInfo): Option[ProjectStackRepl] = {
    projectRepls.get(stackComponentInfo).filter(_.available)
  }

  def getRunningProjectRepls: Iterable[ProjectStackRepl] = {
    projectRepls.values.filter(_.available)
  }

  def getGlobalRepl: GlobalStackRepl = globalRepl

  private def findProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (HaskellProjectUtil.isSourceFile(psiFile)) {
      if (StackProjectManager.isInitializing(project)) {
        HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileInitializing(project)
        None
      } else {
        val componentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
        componentInfo match {
          case Some(ci) => Some(getProjectRepl(ci))
          case None =>
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"No Haskell support for file `${psiFile.getName}` because no Stack target could be found for this file")
            None
        }
      }
    } else {
      None
    }
  }

  private def getProjectRepl(componentInfo: StackComponentInfo): ProjectStackRepl = {
    projectRepls.get(componentInfo) match {
      case Some(repl) => repl
      case None =>
        componentInfo.synchronized {
          projectRepls.get(componentInfo) match {
            case Some(r) => r
            case None =>
              val repl = createAndStartProjectRepl(componentInfo)
              projectRepls.put(componentInfo, repl)
              repl
          }
        }
    }
  }

  private def createAndStartProjectRepl(componentInfo: StackComponentInfo): ProjectStackRepl = {
    val repl = new ProjectStackRepl(project, componentInfo, HaskellSettingsState.getReplTimeout)
    if (!project.isDisposed) {
      repl.start()
    }
    repl
  }
}