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
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.{HaskellFileUtil, OSUtil, Util}

private[component] object LoadComponent {

  private final val ProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r

  def load(psiFile: PsiFile, refreshCache: Boolean, postLoadAction: => Unit): LoadResult = {
    val project = psiFile.getProject
    val loadOutput = StackReplsManager.getProjectRepl(project).load(psiFile)

    val loadFailed = loadOutput.stdOutLines.lastOption.exists(_.contains("Failed, "))
    if (refreshCache && !loadFailed) {
      ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
        override def run(): Unit = {
          postLoadAction
        }
      })
    }

    val filePath = HaskellFileUtil.makeFilePathAbsolute(HaskellFileUtil.getFilePath(psiFile), project)

    // `distinct` because of https://github.com/commercialhaskell/intero/issues/258
    val loadProblems = loadOutput.stdErrLines.distinct.map(l => parseErrorOutputLine(filePath, l))

    val currentFileProblems = loadProblems.flatMap(convertToLoadProblemInCurrentFile)
    val otherFileProblems = loadProblems.diff(currentFileProblems)
    LoadResult(currentFileProblems, otherFileProblems, loadFailed)
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
  def normalizedMessage: String = {
    message.trim.replace(OSUtil.LineSeparator, ' ').replaceAll("\\s+", " ")
  }

  def htmlMessage: String = {
    Util.escapeString(message.replace(' ', '\u00A0'))
  }
}

case class LoadProblemInCurrentFile(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemInOtherFile(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemWithoutLocation(private val message: String) extends LoadProblem(message)
