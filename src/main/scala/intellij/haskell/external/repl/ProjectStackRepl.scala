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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.util.HaskellFileUtil

import scala.collection.JavaConverters._

class ProjectStackRepl(project: Project, replType: StanzaType, target: String, var sourceDirs: Seq[String]) extends StackRepl(project, Some(replType), Some(target), Seq()) {

  def clearLoadedInfo(): Unit = {
    loadedPsiFileInfo = None
    allLoadedPsiFileInfos.clear()
  }

  private case class PsiFileInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedPsiFileInfo: Option[PsiFileInfo] = None

  private case class FileInfo(loadFailed: Boolean)

  private[this] val allLoadedPsiFileInfos = new ConcurrentHashMap[PsiFile, FileInfo]().asScala

  @volatile
  private var isLoading = false

  @volatile
  private var isFindingAvailableLibraryModuleNames = false

  @volatile
  private var isExecutingWithLoad = false

  def isBusy: Boolean = {
    isLoading || isFindingAvailableLibraryModuleNames || isExecutingWithLoad
  }

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getAbsoluteFilePath(psiFile)
    findInfoForCommand(psiFile, s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getAbsoluteFilePath(psiFile)
    findInfoForCommand(psiFile, s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
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

  def load(psiFile: PsiFile): Option[(StackReplOutput, Boolean)] = {
    val filePath = getFilePath(psiFile)
    this.synchronized {
      val output =
        try {
          isLoading = true
          execute(s":load $filePath")
        } finally {
          isLoading = false
        }
      output match {
        case Some(o) =>
          val loadFailed = isLoadFailed(o)
          loadedPsiFileInfo = Some(PsiFileInfo(psiFile, loadFailed))
          allLoadedPsiFileInfos.put(psiFile, FileInfo(loadFailed))
          Some(o, loadFailed)
        case _ =>
          loadedPsiFileInfo = None
          None
      }
    }
  }

  def getModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    findInfoForCommand(psiFile, s":browse! $moduleName")
  }

  // Only returns exported identifiers of current file
  def getModuleIdentifiersWithLoad(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = {
    executeWithLoad(psiFile, s":browse! $moduleName", Some(moduleName))
  }

  def findAvailableLibraryModuleNames(project: Project): Option[StackReplOutput] = synchronized {
    try {
      isFindingAvailableLibraryModuleNames = true
      execute(":load")
      loadedPsiFileInfo = None
      execute(""":complete repl "import " """)
    } finally {
      isFindingAvailableLibraryModuleNames = false
    }
  }

  def showActiveLanguageFlags: Option[StackReplOutput] = synchronized {
    execute(":show language")
  }

  private def findInfoForCommand(psiFile: PsiFile, command: String) = {
    allLoadedPsiFileInfos.get(psiFile) match {
      case Some(lf) if lf.loadFailed => Some(StackReplOutput())
      case Some(_) =>
        this.synchronized {
          execute(command)
        }
      case None =>
        executeWithLoad(psiFile, command)
    }
  }

  private def executeWithLoad(psiFile: PsiFile, command: String, moduleName: Option[String] = None): Option[StackReplOutput] = synchronized {
    def exec(command: String) = {
      try {
        isExecutingWithLoad = true
        execute(command)
      } finally {
        isExecutingWithLoad = false
      }
    }

    loadedPsiFileInfo match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed =>
        exec(command)
      case Some(info) if info.psiFile == psiFile && info.loadFailed => Some(StackReplOutput())
      case _ =>
        load(psiFile)
        loadedPsiFileInfo match {
          case None => None
          case Some(info) if info.psiFile == psiFile && !info.loadFailed =>
            exec(command)
          case _ => Some(StackReplOutput())
        }
    }
  }

  private def isLoadFailed(output: StackReplOutput): Boolean = {
    output.stdOutLines.lastOption.exists(_.contains("Failed, "))
  }

  private def getFilePath(psiFile: PsiFile): String = {
    HaskellFileUtil.getAbsoluteFilePath(psiFile)
  }
}


sealed trait IsFileLoaded

case object Loaded extends IsFileLoaded

case object Failed extends IsFileLoaded

case object NoFileIsLoaded extends IsFileLoaded

case object OtherFileIsLoaded extends IsFileLoaded
