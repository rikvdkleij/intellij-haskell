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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.util.HaskellFileUtil

import scala.collection.JavaConverters._

class ProjectStackRepl(project: Project, stackComponentInfo: StackComponentInfo, replTimeout: Int) extends StackRepl(project, Some(stackComponentInfo), Seq(), replTimeout: Int) {

  import intellij.haskell.external.repl.ProjectStackRepl._

  val target: String = stackComponentInfo.target

  val stanzaType: StackRepl.StanzaType = stackComponentInfo.stanzaType

  val packageName: String = stackComponentInfo.packageName

  def clearLoadedModules(): Unit = {
    loadedModule = None
    loadedDependentModules.clear()
  }

  def clearLoadedModule(): Unit = {
    loadedModule = None
  }

  private case class ModuleInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedModule: Option[ModuleInfo] = None

  private case class DependentModuleInfo(loadFailed: Boolean)

  private type ModuleName = String
  private[this] val loadedDependentModules = new ConcurrentHashMap[ModuleName, DependentModuleInfo]().asScala

  @volatile
  private var busy = false

  def isBusy: Boolean = {
    busy
  }

  def findTypeInfo(moduleName: Option[String], psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = getFilePath(psiFile)
    findInfoForCommand(moduleName, psiFile, s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression", setBusy = false)
  }

  def findLocationInfo(moduleName: Option[String], psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = getFilePath(psiFile)
    findInfoForCommand(moduleName, psiFile, s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression", setBusy = false)
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = {
    executeWithLoad(psiFile, s":info $name")
  }

  def isModuleLoaded(moduleName: String): Boolean = {
    loadedDependentModules.get(moduleName).isDefined
  }

  def isFileLoaded(psiFile: PsiFile): IsFileLoaded = {
    loadedModule match {
      case Some(info) if psiFile == info.psiFile && !info.loadFailed => Loaded
      case Some(info) if psiFile == info.psiFile && info.loadFailed => Failed
      case Some(_) => OtherFileIsLoaded
      case None => NoFileIsLoaded
    }
  }

  private final val OkModulesLoaded = "Ok, modules loaded: "

  def load(psiFile: PsiFile, fileChanged: Boolean): Option[(StackReplOutput, Boolean)] = {
    val filePath = getFilePath(psiFile)
    val reload = if (fileChanged) {
      val loaded = isFileLoaded(psiFile)
      loaded == Loaded || loaded == Failed
    } else {
      HaskellNotificationGroup.logInfoEvent(project, s"No :reload of file ${psiFile.getName} because this file is not changed")
      false
    }
    synchronized {
      val output = if (reload) {
        executeWithSettingBusy(Seq(s":reload"), load = Some(psiFile))
      } else {
        executeWithSettingBusy(Seq(s":load $filePath"), load = Some(psiFile))
      }
      output match {
        case Some(o) =>
          val loadFailed = isLoadFailed(o)
          val loadedModuleNames = o.stdoutLines.find(l => l.startsWith(OkModulesLoaded)).map(findLoadedModuleNames).getOrElse(Array())
          loadedModuleNames.foreach(mn => loadedDependentModules.put(mn, DependentModuleInfo(loadFailed)))

          loadedModule = Some(ModuleInfo(psiFile, loadFailed))
          Some(o, loadFailed)
        case _ =>
          loadedDependentModules.clear()
          loadedModule = None
          None
      }
    }
  }

  private def findLoadedModuleNames(line: String): Array[String] = {
    if (line == "none") {
      Array()
    } else {
      line.replace(OkModulesLoaded, "").init.split(",").map(_.trim)
    }
  }

  def getModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    findInfoForCommand(Some(moduleName), psiFile, s":browse! $moduleName", setBusy = true)
  }

  def getLocalModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    executeWithLoad(psiFile, s":browse! *$moduleName", Some(moduleName))
  }

  // To retrieve only library module names, it will first execute `load` to remove all modules from scope
  def findAvailableLibraryModuleNames(project: Project): Option[StackReplOutput] = synchronized {
    loadedModule = None
    executeWithSettingBusy(Seq(":load", """:complete repl "import " """))
  }

  def showActiveLanguageFlags: Option[StackReplOutput] = synchronized {
    executeWithSettingBusy(Seq(":show language"))
  }

  override def restart(forceExit: Boolean): Unit = synchronized {
    if (available && !starting) {
      exit(forceExit)
      clearLoadedModules()
      start()
    }
  }

  private def findInfoForCommand(moduleName: Option[String], psiFile: PsiFile, command: String, setBusy: Boolean): Option[StackReplOutput] = synchronized {
    moduleName.flatMap(loadedDependentModules.get) match {
      case Some(lf) if lf.loadFailed => Some(StackReplOutput())
      case Some(_) =>
        if (setBusy) {
          executeWithSettingBusy(Seq(command))
        } else {
          executeWithoutSettingBusy(Seq(command))
        }
      case None =>
        executeWithLoad(psiFile, command)
    }
  }

  private def executeWithLoad(psiFile: PsiFile, command: String, moduleName: Option[String] = None): Option[StackReplOutput] = synchronized {
    loadedModule match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeWithSettingBusy(Seq(command))
      case Some(info) if info.psiFile == psiFile && info.loadFailed => Some(StackReplOutput())
      case _ =>
        load(psiFile, fileChanged = false)
        loadedModule match {
          case None => None
          case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeWithSettingBusy(Seq(command))
          case _ => Some(StackReplOutput())
        }
    }
  }

  private def executeWithSettingBusy(commands: Seq[String], load: Option[PsiFile] = None) = {
    try {
      busy = true
      executeWithoutSettingBusy(commands)
    } finally {
      busy = false
    }
  }

  private def executeWithoutSettingBusy(commands: Seq[String]) = {
    commands.map(c => execute(c)).lastOption.flatten
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
      case None => throw new IllegalStateException(s"Can not load file `${psiFile.getName}` in REPL because it exists only in memory")
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
