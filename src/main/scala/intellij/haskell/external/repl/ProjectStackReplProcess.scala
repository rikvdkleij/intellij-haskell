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

  private[this] var loadedPsiFileInfo: Option[LoadedPsiFileInfo] = None

  override def getComponentName: String = "project-stack-repl"

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    checkFileIsLoadedAndExecuteWithFilePath(psiFile, filePath => execute(s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    checkFileIsLoadedAndExecuteWithFilePath(psiFile, filePath => execute(s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = synchronized {
    checkFileIsLoadedAndExecuteWithFilePath(psiFile, _ => execute(s":info $name"))
  }

  def load(psiFile: PsiFile): Option[(StackReplOutput, Boolean)] = synchronized {
    val filePath = HaskellFileUtil.getFilePath(psiFile)
    execute(s":load $filePath") match {
      case Some(output) =>
        val loadFailed = isLoadFailed(output)
        loadedPsiFileInfo = Some(LoadedPsiFileInfo(psiFile, loadFailed))
        Some(output, loadFailed)
      case _ =>
        loadedPsiFileInfo = None
        None
    }
  }

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    checkFileIsLoadedAndExecute(None, moduleName, execute(s":browse! $moduleName"))
  }

  def getAllTopLevelModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = synchronized {
    checkFileIsLoadedAndExecute(Some(psiFile), moduleName, execute(s":browse! *$moduleName"))
  }

  def findAllAvailableLibraryModules: Option[Iterable[String]] = synchronized {
    execute(":load")
    loadedPsiFileInfo = None
    execute(""":complete repl "import " """).map(findModuleNames)
  }

  private def findModuleNames(output: StackReplOutput) = {
    val lines = output.stdOutLines
    if (lines.isEmpty) {
      Iterable()
    } else {
      lines.tail.map(m => m.substring(1, m.length - 1))
    }
  }

  private def checkFileIsLoadedAndExecuteWithFilePath(psiFile: PsiFile, executeAction: String => Option[StackReplOutput]): Option[StackReplOutput] = {
    if (loadedPsiFileInfo.isEmpty || loadedPsiFileInfo.exists(i => i.psiFile != psiFile)) {
      load(psiFile)
    }
    if (loadedPsiFileInfo.exists(i => i.psiFile == psiFile && !i.loadFailed)) {
      val filePath = HaskellFileUtil.getFilePath(psiFile)
      executeAction(filePath)
    } else {
      None
    }
  }

  private def checkFileIsLoadedAndExecute(psiFile: Option[PsiFile], moduleName: String, executeAction: => Option[StackReplOutput]): Option[StackReplOutput] = {
    psiFile match {
      case None =>
        val output = execute(s":load $moduleName")
        loadedPsiFileInfo = None // Always to None because no way to determine unambiguously file
        output match {
          case Some(o) =>
            if (isLoadFailed(o)) {
              None
            } else {
              executeAction
            }
          case None => None
        }

      case Some(pf) if loadedPsiFileInfo.isEmpty || loadedPsiFileInfo.exists(_.psiFile != pf) =>
        load(pf) match {
          case Some((lf, failed)) if !failed => executeAction
          case _ => None
        }
      case _ => executeAction
    }
  }

  private def isLoadFailed(output: StackReplOutput): Boolean = {
    output.stdOutLines.lastOption.exists(_.contains("Failed, "))
  }
}
