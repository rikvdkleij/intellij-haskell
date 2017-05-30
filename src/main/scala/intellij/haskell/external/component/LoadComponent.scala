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
import intellij.haskell.external.repl._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileUtil, StringUtil}

private[component] object LoadComponent {

  private final val ProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r

  def isLoaded(psiFile: PsiFile, forceLoad: Boolean): Boolean = {
    val project = psiFile.getProject
    val projectRepl = StackReplsManager.getProjectRepl(project)
    projectRepl.map(_.isLoaded(psiFile)).exists {
      case Loaded() => true
      case Failed() => false
      case _ => if (forceLoad) !load(psiFile).loadFailed else false
    }
  }

  def load(psiFile: PsiFile): LoadResult = {
    val project = psiFile.getProject

    StackReplsManager.getProjectRepl(project).flatMap(_.load(psiFile)) match {
      case Some((loadOutput, loadFailed)) =>

        if (!loadFailed) {
          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
            override def run(): Unit = {
              TypeInfoComponent.invalidate(psiFile)
              DefinitionLocationComponent.invalidate(psiFile)
              NameInfoComponent.invalidate(psiFile)
              val moduleName = HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
              moduleName.foreach(BrowseModuleComponent.invalidateForModule(project, _, psiFile))
            }
          })
        }

        val filePath = HaskellFileUtil.makeFilePathAbsolute(HaskellFileUtil.getAbsoluteFilePath(psiFile), project)

        // `distinct` because of https://github.com/commercialhaskell/intero/issues/258
        val loadProblems = loadOutput.stdErrLines.distinct.map(l => parseErrorOutputLine(filePath, l))

        val currentFileProblems = loadProblems.flatMap(convertToLoadProblemInCurrentFile)
        val otherFileProblems = loadProblems.diff(currentFileProblems)

        LoadResult(currentFileProblems, otherFileProblems, loadFailed)
      case _ => LoadResult()
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
        val displayMessage = message.trim.replaceAll("""(\s\s\s\s+)""", "\n" + "$1")
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
    message.split("\n").mkString.replaceAll("\\s+", " ")
  }

  def htmlMessage: String = {
    StringUtil.escapeString(message.replace(' ', '\u00A0'))
  }

  def isWarning: Boolean = {
    message.startsWith("warning:") || message.startsWith("Warning:")
  }
}

case class LoadProblemInCurrentFile private(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemInOtherFile private(filePath: String, lineNr: Int, columnNr: Int, private val message: String) extends LoadProblem(message)

case class LoadProblemWithoutLocation private(private val message: String) extends LoadProblem(message)
