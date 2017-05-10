/*
 * Copyright 2016 Rik van der Kleij
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
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.util.index.HaskellModuleIndex


private[repl] class ProjectStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--test"), true) {

  private case class PsiFileInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedPsiFileInfo: Option[PsiFileInfo] = None

  case class FileInfo(moduleName: Option[String], loadFailed: Boolean)

  private[this] val allLoadedPsiFiles = new ConcurrentHashMap[PsiFile, FileInfo]()

  override def getComponentName: String = "project-stack-repl"

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getFilePath(psiFile)
    findInfoForCommand(psiFile, s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = {
    val filePath = HaskellFileUtil.getFilePath(psiFile)
    findInfoForCommand(psiFile, s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression")
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = synchronized {
    executeWithLoad(psiFile, execute(s":info $name"))
  }

  def isLoaded(psiFile: PsiFile): IsFileLoaded = {
    loadedPsiFileInfo match {
      case Some(info) if psiFile == info.psiFile && !info.loadFailed => Loaded()
      case Some(info) if psiFile == info.psiFile && info.loadFailed => Failed()
      case Some(_) => OtherFileIsLoaded()
      case None => NoFileIsLoaded()
    }
  }

  def load(psiFile: PsiFile): Option[(StackReplOutput, Boolean)] = synchronized {
    val filePath = getFilePath(psiFile)
    val output = execute(s":load $filePath")

    output match {
      case Some(o) =>
        val loadFailed = isLoadFailed(o)
        loadedPsiFileInfo = Some(PsiFileInfo(psiFile, loadFailed))
        allLoadedPsiFiles.put(psiFile, FileInfo(HaskellPsiUtil.findModuleName(psiFile, runInRead = true), loadFailed))
        Some(o, loadFailed)
      case _ =>
        loadedPsiFileInfo = None
        None
    }
  }

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = {
    val file = ApplicationManager.getApplication.runReadAction(new Computable[Option[PsiFile]] {
      override def compute(): Option[PsiFile] = {
        HaskellModuleIndex.getFilesByModuleName(project, moduleName, GlobalSearchScope.projectScope(project)).headOption.map(_.asInstanceOf[PsiFile])
      }
    })
    file.flatMap(f => findInfoForCommand(f, s":browse! $moduleName"))
  }

  def getAllTopLevelModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = synchronized {
    executeWithLoad(psiFile, execute(s":browse! *$moduleName"))
  }

  def findAllAvailableLibraryModules: Option[Iterable[String]] = synchronized {
    execute(":load")
    loadedPsiFileInfo = None
    execute(""":complete repl "import " """).map(findModuleNames)
  }

  private def findInfoForCommand(psiFile: PsiFile, command: String) = {
    Option(allLoadedPsiFiles.get(psiFile)) match {
      case Some(lf) if lf.loadFailed => Some(StackReplOutput())
      case Some(_) =>
        synchronized {
          execute(command)
        }
      case None =>
        synchronized {
          executeWithLoad(psiFile, execute(command))
        }
    }
  }

  private def findModuleNames(output: StackReplOutput) = {
    val lines = output.stdOutLines
    if (lines.isEmpty) {
      Iterable()
    } else {
      lines.tail.map(m => m.substring(1, m.length - 1))
    }
  }

  private def executeWithLoad(psiFile: PsiFile, executeAction: => Option[StackReplOutput]): Option[StackReplOutput] = {
    def execute: Option[StackReplOutput] = {
      loadedPsiFileInfo match {
        case None => None
        case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeAction
        case _ => Some(StackReplOutput())
      }
    }

    loadedPsiFileInfo match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeAction
      case Some(info) if info.psiFile == psiFile && info.loadFailed => Some(StackReplOutput())
      case _ =>
        load(psiFile)
        execute
    }
  }

  private def isLoadFailed(output: StackReplOutput): Boolean = {
    output.stdOutLines.lastOption.exists(_.contains("Failed, "))
  }

  private def getFilePath(psiFile: PsiFile): String = {
    HaskellFileUtil.getFilePath(psiFile)
  }
}


sealed trait IsFileLoaded

case class Loaded() extends IsFileLoaded

case class Failed() extends IsFileLoaded

case class NoFileIsLoaded() extends IsFileLoaded

case class OtherFileIsLoaded() extends IsFileLoaded