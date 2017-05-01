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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.util.HaskellFileUtil

private[repl] class ProjectStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--test"), true) {

  private case class LoadedPsiFileInfo(psiFile: PsiFile, loadFailed: Boolean)

  @volatile
  private[this] var loadedPsiFileInfo: Option[LoadedPsiFileInfo] = None

  @volatile
  private[this] var busy = false

  override def getComponentName: String = "project-stack-repl"

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    executeWithLoad(psiFile, filePath => execute(s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    executeWithLoad(psiFile, filePath => execute(s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = synchronized {
    executeWithLoad(psiFile, _ => execute(s":info $name"))
  }

  def isLoaded(psiFile: PsiFile): IsFileLoaded = {
    loadedPsiFileInfo match {
      case Some(info) if psiFile == info.psiFile && !info.loadFailed => Loaded()
      case Some(info) if psiFile == info.psiFile && info.loadFailed => Failed()
      case Some(_) => OtherFileIsLoaded()
      case None => NoFileIsLoaded()
    }
  }

  def isBusy: Boolean = {
    busy
  }

  def load(psiFile: PsiFile): Option[(StackReplOutput, Boolean)] = synchronized {
    busy = true
    try {
      val filePath = getFilePath(psiFile)
      execute(s":load $filePath") match {
        case Some(output) =>
          val loadFailed = isLoadFailed(output)
          loadedPsiFileInfo = Some(LoadedPsiFileInfo(psiFile, loadFailed))
          Some(output, loadFailed)
        case _ =>
          loadedPsiFileInfo = None
          None
      }
    } finally {
      busy = false
    }
  }

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    busy = true
    try {
      execute(s":browse! $moduleName")
    } finally {
      busy = false
    }
  }

  def getAllTopLevelModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = synchronized {
    busy = true
    try {
      executeWithLoad(psiFile, _ => execute(s":browse! *$moduleName"), Some(moduleName))
    } finally {
      busy = false
    }
  }

  def findAllAvailableLibraryModules: Option[Iterable[String]] = synchronized {
    busy = true
    try {
      execute(":load")
      loadedPsiFileInfo = None
      execute(""":complete repl "import " """).map(findModuleNames)
    } finally {
      busy = false
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

  private def executeWithLoad(psiFile: PsiFile, executeAction: String => Option[StackReplOutput], moduleName: Option[String] = None): Option[StackReplOutput] = {
    def execute: Option[StackReplOutput] = {
      loadedPsiFileInfo match {
        case None => None
        case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeAction(getFilePath(psiFile))
        case _ => Some(StackReplOutput())
      }
    }

    loadedPsiFileInfo match {
      case Some(info) if info.psiFile == psiFile && !info.loadFailed => executeAction(getFilePath(psiFile))
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