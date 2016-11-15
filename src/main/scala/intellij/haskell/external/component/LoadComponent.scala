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

package intellij.haskell.external.component

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileUtil, OSUtil, StringUtil}

private[component] object LoadComponent {

  private final val ProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r

  def load(psiFile: PsiFile, refreshCache: Boolean): LoadResult = {
    val project = psiFile.getProject

    StackReplsManager.getProjectRepl(project).load(psiFile) match {
      case Some((loadOutput, loadFailed)) =>
        if (refreshCache && !loadFailed) {
          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
            override def run(): Unit = {
              findModuleName(psiFile).foreach(BrowseModuleComponent.refreshForModule(project, _, psiFile))
              NameInfoComponent.markAllToRefresh(psiFile)
              TypeInfoComponent.markAllToRefresh(psiFile)
              DefinitionLocationComponent.markAllToRefresh(psiFile)
            }
          })
        }

        val filePath = HaskellFileUtil.makeFilePathAbsolute(HaskellFileUtil.getFilePath(psiFile), project)

        // `distinct` because of https://github.com/commercialhaskell/intero/issues/258
        val loadProblems = loadOutput.stdErrLines.distinct.map(l => parseErrorOutputLine(filePath, l))

        val currentFileProblems = loadProblems.flatMap(convertToLoadProblemInCurrentFile)
        val otherFileProblems = loadProblems.diff(currentFileProblems)
        LoadResult(currentFileProblems, otherFileProblems, loadFailed)
      case _ => LoadResult()
    }
  }

  private def findModuleName(psiFile: PsiFile) = {
    ApplicationManager.getApplication.runReadAction {
      new Computable[Option[String]] {
        override def compute(): Option[String] = {
          HaskellPsiUtil.findModuleName(psiFile)
        }
      }
    }
  }

  private def convertToLoadProblemInCurrentFile(loadProblem: LoadProblem) = {
    loadProblem match {
      case lp: LoadProblemInCurrentFile => Some(lp)
      case _ => None
    }
  }

  private[component] def parseErrorOutputLine(filePath: String, outputLine: String): LoadProblem = {
    outputLine match {
      case ProblemPattern(problemFilePath, lineNr, columnNr, message) =>
        val displayMessage = (if (message.startsWith("    ")) {
          s"Error: $message"
        } else {
          message
        }).replaceAll("""(\s\s\s\s+)""", OSUtil.LineSeparator + "$1")
        if (filePath == problemFilePath) {
          LoadProblemInCurrentFile(problemFilePath, lineNr.toInt, columnNr.toInt, displayMessage)
        } else {
          LoadProblemInOtherFile(problemFilePath, lineNr.toInt, columnNr.toInt, displayMessage)
        }
      case m => LoadProblemWithoutLocation(m)
    }
  }
}

case class LoadResult(currentFileProblems: Iterable[LoadProblemInCurrentFile] = Iterable(), otherFileProblems: Iterable[LoadProblem] = Iterable(), loadFailed: Boolean = false)

sealed abstract class LoadProblem(private val message: String) {
  def plainMessage: String = {
    message.split(OSUtil.LineSeparator).tail.mkString.trim.replaceAll("\\s+", " ")
  }

  def htmlMessage: String = {
    StringUtil.escapeString(message.split(OSUtil.LineSeparator).tail.mkString("\n").trim.replace(' ', '\u00A0'))
  }

  def isWarning: Boolean = {
    message.trim.startsWith("warning:") || message.trim.startsWith("Warning:")
  }
}

case class LoadProblemInCurrentFile private(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemInOtherFile private(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemWithoutLocation private(private val message: String) extends LoadProblem(message)
