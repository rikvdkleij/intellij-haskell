/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.util.{HaskellFileUtil, ScalaFutureUtil}

import scala.concurrent.{Future, blocking}
import scala.jdk.CollectionConverters._

case class ProjectStackRepl(project: Project, stackComponentInfo: StackComponentInfo, replTimeout: Int) extends StackRepl(project, Some(stackComponentInfo), Seq(), replTimeout: Int) {

  import intellij.haskell.external.repl.ProjectStackRepl._

  val target: String = stackComponentInfo.target

  val stanzaType: StackRepl.StanzaType = stackComponentInfo.stanzaType

  val packageName: String = stackComponentInfo.packageName

  def clearLoadedModules(): Unit = {
    loadedFile = None
    loadedDependentModules.clear()
    everLoadedDependentModules.clear()
  }

  def clearLoadedModule(): Unit = {
    loadedFile = None
  }

  private case class ModuleInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedFile: Option[ModuleInfo] = None

  private case class DependentModuleInfo()

  private case class LoadedModuleInfo(info: Option[(StackReplOutput, Boolean)])

  private type ModuleName = String
  private[this] val loadedDependentModules = new ConcurrentHashMap[ModuleName, DependentModuleInfo]().asScala
  private[this] val everLoadedDependentModules = new ConcurrentHashMap[ModuleName, DependentModuleInfo]().asScala
  private[this] val everLoadedInfo = new ConcurrentHashMap[String, LoadedModuleInfo]().asScala

  import scala.concurrent.ExecutionContext.Implicits.global

  def findTypeInfo(moduleName: Option[String], psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = getFilePath(psiFile)

    def execute = {
      blocking {
        executeModuleLoadedCommand(moduleName, psiFile, s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
      }
    }

    ScalaFutureUtil.waitForValue(project, Future(execute), ":type-at in ProjectStackRepl").flatten
  }

  def findLocationInfo(moduleName: Option[String], psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = getFilePath(psiFile)

    def execute = {
      blocking {
        executeModuleLoadedCommand(moduleName, psiFile, s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
      }
    }

    ScalaFutureUtil.waitForValue(project, Future(execute), ":loc-at in ProjectStackRepl").flatten
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = {
    def execute = {
      blocking {
        executeWithLoad(psiFile, s":info $name")
      }
    }

    ScalaFutureUtil.waitForValue(psiFile.getProject, Future(execute), ":info in ProjectStackRepl").flatten
  }

  def isModuleLoaded(moduleName: String): Boolean = {
    everLoadedDependentModules.contains(moduleName)
  }

  def isBrowseModuleLoaded(moduleName: String): Boolean = {
    loadedDependentModules.contains(moduleName)
  }

  def isFileLoaded(psiFile: PsiFile): IsFileLoaded = {
    loadedFile match {
      case Some(info) if psiFile == info.psiFile && !info.loadFailed => Loaded
      case Some(info) if psiFile == info.psiFile && info.loadFailed => Failed
      case Some(_) => OtherFileIsLoaded
      case None => NoFileIsLoaded
    }
  }

  private def setLoadedModules(): Unit = {
    loadedDependentModules.clear()
    execute(":show modules") match {
      case Some(output) =>
        val loadedModuleNames = output.stdoutLines.map(l => l.takeWhile(_ != ' '))
        loadedModuleNames.foreach(mn => loadedDependentModules.put(mn, DependentModuleInfo()))
        loadedModuleNames.foreach(mn => everLoadedDependentModules.put(mn, DependentModuleInfo()))
      case None => ()
    }
  }

  def load(psiFile: PsiFile, fileModified: Boolean, forceNoReload: Boolean = false): Option[(StackReplOutput, Boolean)] = synchronized {
    val reload = if (forceNoReload) {
      false
    } else if (fileModified) {
      val loaded = isFileLoaded(psiFile)
      loaded == Loaded || loaded == Failed
    } else {
      false
    }

    if (!fileModified && !forceNoReload && everLoadedInfo.contains(getFilePath(psiFile))) {
      everLoadedInfo(getFilePath(psiFile)).info
    } else {
      val output = if (reload) {
        execute(s":reload")
      } else {
        val filePath = getFilePath(psiFile)
        execute(s":load *$filePath")
      }

      output match {
        case Some(o) =>
          val loadFailed = isLoadFailed(o)
          setLoadedModules()

          loadedFile = Some(ModuleInfo(psiFile, loadFailed))
          everLoadedInfo.put(getFilePath(psiFile), LoadedModuleInfo(Some((o, loadFailed))))
          Some(o, loadFailed)
        case _ =>
          loadedDependentModules.clear()
          everLoadedInfo.remove(getFilePath(psiFile))
          loadedFile = None
          None
      }
    }
  }

  def getModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile]): Option[StackReplOutput] = {
    ScalaFutureUtil.waitForValue(project,
      Future {
        blocking {
          synchronized {
            if (psiFile.isEmpty || isBrowseModuleLoaded(moduleName) || psiFile.exists(pf => load(pf, fileModified = false).exists(_._2 == false))) {
              execute(s":browse! $moduleName")
            } else {
              HaskellNotificationGroup.logInfoEvent(project, s"Couldn't get module identifiers for module $moduleName because file ${psiFile.map(_.getName).getOrElse("-")} isn't loaded")
              None
            }
          }
        }
      }, "getModuleIdentifiers in ProjectStackRepl").flatten
  }

  override def restart(forceExit: Boolean): Unit = synchronized {
    if (available && !starting) {
      exit(forceExit)
      start()
    }
  }

  private def executeModuleLoadedCommand(moduleName: Option[String], psiFile: PsiFile, command: String): Option[StackReplOutput] = synchronized {
    if (moduleName.exists(isModuleLoaded)) {
      execute(command)
    } else {
      executeWithLoad(psiFile, command)
    }
  }

  private def executeWithLoad(psiFile: PsiFile, command: String): Option[StackReplOutput] = synchronized {
    loadedFile match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed => execute(command)
      case _ =>
        load(psiFile, fileModified = false)
        loadedFile match {
          case None => None
          case Some(info) if info.psiFile == psiFile => execute(command)
          case _ => None
        }
    }
  }

  private def isLoadFailed(output: StackReplOutput): Boolean = {
    output.stdoutLines.lastOption.exists(_.contains("Failed, "))
  }

  private def getFilePath(psiFile: PsiFile): String = {
    HaskellFileUtil.getAbsolutePath(psiFile) match {
      case Some(filePath) =>
        if (filePath.contains(" ")) {
          s""""$filePath""""
        } else {
          filePath
        }
      case None => throw new IllegalStateException(s"Can't load file `${psiFile.getName}` in REPL because it only exists in memory")
    }
  }
}

object ProjectStackRepl {

  sealed trait IsFileLoaded

  case object Loaded extends IsFileLoaded

  case object Failed extends IsFileLoaded

  case object NoFileIsLoaded extends IsFileLoaded

  case object OtherFileIsLoaded extends IsFileLoaded

}
