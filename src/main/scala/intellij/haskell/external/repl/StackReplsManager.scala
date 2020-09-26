/*
 * Copyright 2014-2020 Rik van der Kleij
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
import intellij.haskell.external.component.HaskellComponentsManager.ComponentTarget
import intellij.haskell.external.component._
import intellij.haskell.external.repl.StackRepl._
import intellij.haskell.external.repl.StackReplsManager.ProjectReplTargets
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._

import scala.jdk.CollectionConverters._

private[external] object StackReplsManager {

  case class ProjectReplTargets(stanzaType: StanzaType, targets: Seq[ComponentTarget]) {
    def targetsName: String = targets.map(_.target).mkString(" ")
  }

  def getReplsManager(project: Project): Option[StackReplsManager] = {
    StackProjectManager.getStackProjectManager(project).flatMap(_.getStackReplsManager)
  }

  def getRunningProjectRepls(project: Project): Iterable[ProjectStackRepl] = {
    getReplsManager(project).map(_.getRunningProjectRepls).getOrElse(Iterable())
  }

  def getProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    getReplsManager(psiFile.getProject).flatMap(_.findProjectRepl(psiFile))
  }

  def getProjectRepl(project: Project, projectReplTargets: ProjectReplTargets): Option[ProjectStackRepl] = {
    getReplsManager(project).map(_.getProjectRepl(projectReplTargets))
  }

  def getGlobalRepl(project: Project): Option[GlobalStackRepl] = {
    val repl = getReplsManager(project).map(_.getGlobalRepl)
    repl.foreach(r => if (!r.available && !r.starting) r.start())
    repl
  }

  def getGlobalRepl2(project: Project): Option[GlobalStackRepl] = {
    val repl = getReplsManager(project).map(_.getGlobalRepl2)
    repl.foreach(r => if (!r.available && !r.starting) r.start())
    repl
  }

  private def createPackageInfos(project: Project): Iterable[(Module, PackageInfo)] = {
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
        ci <- PackageInfo.create(project, cf)
      } yield (m, ci)
      if (cabalFiles.isEmpty) {
        HaskellNotificationGroup.logWarningBalloonEvent(project, s"No Cabal files found for project `${project.getName}`. Check your project configuration.")
      }
      cabalFiles
    }
  }

  private def createComponentTargets(moduleCabalInfos: Iterable[(Module, PackageInfo)]): Iterable[ComponentTarget] = {
    moduleCabalInfos.flatMap {
      case (m: Module, cabalInfo: PackageInfo) => cabalInfo.cabalStanzas.map {
        case cs: LibraryCabalStanza => ComponentTarget(m, cs.modulePath, cs.packageName, cs.targetName, LibType, cs.sourceDirs, None, cs.isNoImplicitPreludeActive, cs.buildDepends, cs.exposedModuleNames)
        case cs: ExecutableCabalStanza => ComponentTarget(m, cs.modulePath, cs.packageName, cs.targetName, ExeType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
        case cs: TestSuiteCabalStanza => ComponentTarget(m, cs.modulePath, cs.packageName, cs.targetName, TestSuiteType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
        case cs: BenchmarkCabalStanza => ComponentTarget(m, cs.modulePath, cs.packageName, cs.targetName, BenchmarkType, cs.sourceDirs, cs.mainIs, cs.isNoImplicitPreludeActive, cs.buildDepends)
      }
    }
  }
}

private[external] class StackReplsManager(val project: Project) {

  private val globalRepl: GlobalStackRepl = GlobalStackRepl(project, HaskellSettingsState.getReplTimeout)
  private val globalRepl2: GlobalStackRepl = GlobalStackRepl(project, HaskellSettingsState.getReplTimeout)

  private val startedTargetProjectRepls = new ConcurrentHashMap[ProjectReplTargets, ProjectStackRepl]().asScala

  val modulePackageInfos: Iterable[(Module, PackageInfo)] = StackReplsManager.createPackageInfos(project)

  val componentTargets: Iterable[ComponentTarget] = StackReplsManager.createComponentTargets(modulePackageInfos)

  val projectReplTargets: Iterable[ProjectReplTargets] = componentTargets.groupBy(_.stanzaType).flatMap { case (stanzaType, targets) =>
    if (stanzaType == LibType) {
      Seq(ProjectReplTargets(stanzaType, targets.toSeq))
    } else {
      targets.map(target => ProjectReplTargets(stanzaType, Seq(target)))
    }
  }

  def getRunningProjectRepls: Iterable[ProjectStackRepl] = {
    startedTargetProjectRepls.values.filter(_.available)
  }

  def libTargetsName: Option[String] = {
    projectReplTargets.find(_.stanzaType == LibType).map(_.targetsName)
  }

  def getGlobalRepl: GlobalStackRepl = globalRepl

  def getGlobalRepl2: GlobalStackRepl = globalRepl2

  def findProjectReplTargets(componentTarget: ComponentTarget): Option[ProjectReplTargets] = {
    projectReplTargets.find(_.targets.contains(componentTarget))
  }

  private def findProjectRepl(psiFile: PsiFile): Option[ProjectStackRepl] = {
    if (HaskellProjectUtil.isSourceFile(psiFile)) {
      val target = HaskellComponentsManager.findStackComponentInfo(psiFile)
      target.flatMap(findProjectReplTargets) match {
        case Some(t) => Some(getProjectRepl(t))
        case None =>
          HaskellNotificationGroup.warningEvent(project, s"No Haskell support for file `${psiFile.getName}` because no component target could be found for this file")
          None
      }
    } else {
      None
    }
  }

  private def getProjectRepl(targets: ProjectReplTargets): ProjectStackRepl = {
    startedTargetProjectRepls.get(targets) match {
      case Some(repl) => repl
      case None =>
        targets.synchronized {
          startedTargetProjectRepls.get(targets) match {
            case Some(r) => r
            case None =>
              val repl = createAndStartProjectRepl(targets)
              startedTargetProjectRepls.put(targets, repl)
              repl
          }
        }
    }
  }

  private def createAndStartProjectRepl(targets: ProjectReplTargets): ProjectStackRepl = {
    val repl = new ProjectStackRepl(project, targets, HaskellSettingsState.getReplTimeout)
    if (!project.isDisposed) {
      repl.start()
    }
    repl
  }
}