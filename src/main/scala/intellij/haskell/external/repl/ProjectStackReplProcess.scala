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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import intellij.haskell.psi.HaskellPsiUtil

private[repl] class ProjectStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--test"), true) {

  private case class LoadedPsiFileInfo(psiFile: Option[PsiFile], moduleName: Option[String], loadFailed: Boolean)

  private[this] var loadedPsiFileInfo: Option[LoadedPsiFileInfo] = None

  override def getComponentName: String = "project-stack-repl"

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    execute(psiFile, filePath => execute(s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): Option[StackReplOutput] = synchronized {
    execute(psiFile, filePath => execute(s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findInfo(psiFile: PsiFile, name: String): Option[StackReplOutput] = synchronized {
    execute(psiFile, _ => execute(s":info $name"))
  }

  def load(psiFile: PsiFile): Option[(StackReplOutput, Boolean)] = synchronized {
    load(psiFile, None)
  }

  private def load(psiFile: PsiFile, moduleName: Option[String]): Option[(StackReplOutput, Boolean)] = {
    val filePath = getFilePath(psiFile)
    val moduleNameAction = ApplicationManager.getApplication.runReadAction(new Computable[Option[String]] {
      override def compute(): Option[String] = {
        moduleName.orElse(HaskellPsiUtil.findModuleName(psiFile))
      }
    })
    execute(s":load $filePath") match {
      case Some(output) =>
        val loadFailed = isLoadFailed(output)
        loadedPsiFileInfo = Some(LoadedPsiFileInfo(Some(psiFile), moduleNameAction, loadFailed))
        Some(output, loadFailed)
      case _ =>
        loadedPsiFileInfo = None
        None
    }
  }

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    execute(None, moduleName, mn => execute(s":browse! $mn"))
  }

  def getAllTopLevelModuleIdentifiers(moduleName: String, psiFile: PsiFile): Option[StackReplOutput] = synchronized {
    execute(Some(psiFile), moduleName, mn => execute(s":browse! *$mn"))
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

  private def execute(psiFile: PsiFile, executeAction: String => Option[StackReplOutput]): Option[StackReplOutput] = {
    def execute: Option[StackReplOutput] = {
      loadedPsiFileInfo match {
        case None => None
        case Some(info) if info.psiFile.contains(psiFile) && !info.loadFailed => executeAction(getFilePath(psiFile))
        case _ => Some(StackReplOutput())
      }
    }

    loadedPsiFileInfo match {
      case Some(info) if info.psiFile.contains(psiFile) && !info.loadFailed => executeAction(getFilePath(psiFile))
      case Some(info) if info.psiFile.contains(psiFile) && info.loadFailed => Some(StackReplOutput())
      case Some(info) => HaskellPsiUtil.findModuleName(psiFile) match {
        case Some(mn) if info.moduleName.contains(mn) && !info.loadFailed => executeAction(mn)
        case Some(mn) if info.moduleName.contains(mn) && info.loadFailed => Some(StackReplOutput())
        case omn =>
          load(psiFile, omn)
          execute
      }
      case _ =>
        load(psiFile)
        execute
    }
  }

  private def execute(psiFile: Option[PsiFile], moduleName: String, executeAction: String => Option[StackReplOutput]): Option[StackReplOutput] = {
    psiFile match {
      case Some(pf) => execute(pf, executeAction)
      case None =>
        loadedPsiFileInfo match {
          case Some(info) if !info.loadFailed && info.moduleName.contains(moduleName) => executeAction(moduleName)
          case Some(info) if info.loadFailed && info.moduleName.contains(moduleName) => Some(StackReplOutput())
          case _ =>
            val output = execute(s":load $moduleName")
            output match {
              case Some(o) =>
                if (isLoadFailed(o)) {
                  loadedPsiFileInfo = Some(LoadedPsiFileInfo(None, Some(moduleName), loadFailed = true))
                  Some(StackReplOutput())
                } else {
                  loadedPsiFileInfo = Some(LoadedPsiFileInfo(None, Some(moduleName), loadFailed = false))
                  executeAction(moduleName)
                }
              case None =>
                loadedPsiFileInfo = None
                None
            }
        }
    }
  }

  private def isLoadFailed(output: StackReplOutput): Boolean = {
    output.stdOutLines.lastOption.exists(_.contains("Failed, "))
  }

  private def getFilePath(psiFile: PsiFile): String = {
    psiFile.getOriginalFile.getVirtualFile.getPath
  }
}
