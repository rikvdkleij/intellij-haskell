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
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.{HaskellFileUtil, ScalaUtil}

import scala.collection.JavaConverters._

class ProjectStackRepl(project: Project, componentInfo: StackComponentInfo, replTimeout: Int) extends StackRepl(project, Some(componentInfo), Seq(), replTimeout: Int) {

  import intellij.haskell.external.repl.ProjectStackRepl._

  def clearLoadedInfo(): Unit = {
    loadedPsiFileInfo = None
    allLoadedPsiFileInfos.clear()
  }

  private case class PsiFileInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedPsiFileInfo: Option[PsiFileInfo] = None

  private case class FileInfo(loadFailed: Boolean)

  private[this] val allLoadedPsiFileInfos = new ConcurrentHashMap[String, FileInfo]().asScala

  @volatile
  private var busy = false

  def isBusy: Boolean = {
    busy
  }

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getAbsolutePath(psiFile)
    findInfoForCommand(psiFile, s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression", setBusy = false)
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getAbsolutePath(psiFile)
    findInfoForCommand(psiFile, s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression", setBusy = false)
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = {
    executeWithLoad(psiFile, s":info $name")
  }

  def isLoaded(psiFile: PsiFile): IsFileLoaded = {
    loadedPsiFileInfo match {
      case Some(info) if psiFile == info.psiFile && !info.loadFailed => Loaded
      case Some(info) if psiFile == info.psiFile && info.loadFailed => Failed
      case Some(_) => OtherFileIsLoaded
      case None => NoFileIsLoaded
    }
  }

  private final val OkModulesLoaded = "Ok, modules loaded: "
  private final val ModuleListRegEx = """([\w\,\s]+)\."""
  private final val OkModulesLoadedPattern = (OkModulesLoaded + ModuleListRegEx).r

  def load(psiFile: PsiFile, reload: Boolean): Option[(StackReplOutput, Boolean)] = {
    val filePath = getFilePath(psiFile)
    synchronized {
      val output = if (reload) {
        executeWithSettingBusy(s":reload")
      } else {
        executeWithSettingBusy(s":load $filePath")
      }
      output match {
        case Some(o) =>
          val loadFailed = isLoadFailed(o)
          o.stdoutLines.find(l => l.startsWith(OkModulesLoaded)).map(findLoadedModuleNames).foreach(_.foreach(mn => allLoadedPsiFileInfos.put(mn, FileInfo(loadFailed))))

          loadedPsiFileInfo = Some(PsiFileInfo(psiFile, loadFailed))
          Some(o, loadFailed)
        case _ =>
          allLoadedPsiFileInfos.clear()
          loadedPsiFileInfo = None
          None
      }
    }
  }

  private def findLoadedModuleNames(line: String): Array[String] = {
    def findModuleNames(moduleNameList: String): Array[String] = {
      if (moduleNameList == "none") Array()
      else moduleNameList.split(",")
    }

    line match {
      case OkModulesLoadedPattern(moduleNameList) => findModuleNames(moduleNameList)
      case _ => Array()
    }
  }

  def getModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    findInfoForCommand(psiFile, s":browse! $moduleName", setBusy = true)
  }

  def getLocalModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    executeWithLoad(psiFile, s":browse! *$moduleName", Some(moduleName))
  }

  // To retrieve only library module names, it will first execute `load` to remove all modules from scope
  def findAvailableLibraryModuleNames(project: Project): Option[StackReplOutput] = synchronized {
    executeWithSettingBusy(":load", """:complete repl "import " """)
  }

  def showActiveLanguageFlags: Option[StackReplOutput] = synchronized {
    executeWithSettingBusy(":show language")
  }

  private def findInfoForCommand(psiFile: PsiFile, command: String, setBusy: Boolean): Option[StackReplOutput] = {
    val moduleName = ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(HaskellFileUtil.getModuleName(psiFile)))
    synchronized {
      moduleName.flatMap(allLoadedPsiFileInfos.get) match {
        case Some(lf) if lf.loadFailed => Some(StackReplOutput())
        case Some(_) =>
          if (setBusy) {
            executeWithSettingBusy(command)
          } else {
            executeWithoutSettingBusy(Seq(command))
          }
        case None =>
          executeWithLoad(psiFile, command)
      }
    }
  }

  private def executeWithLoad(psiFile: PsiFile, command: String, moduleName: Option[String] = None): Option[StackReplOutput] = synchronized {
    loadedPsiFileInfo match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed =>
        executeWithSettingBusy(command)
      case Some(info) if info.psiFile == psiFile && info.loadFailed => Some(StackReplOutput())
      case _ =>
        load(psiFile, reload = false)
        loadedPsiFileInfo match {
          case None => None
          case Some(info) if info.psiFile == psiFile && !info.loadFailed =>
            executeWithSettingBusy(command)
          case _ => Some(StackReplOutput())
        }
    }
  }

  private def executeWithSettingBusy(commands: String*) = {
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
    val filePath = HaskellFileUtil.getAbsolutePath(psiFile)
    if (filePath.contains(" ")) {
      s""""$filePath""""
    } else {
      filePath
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
