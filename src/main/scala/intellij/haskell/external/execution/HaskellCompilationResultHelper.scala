/*
 * Copyright 2014-2019 Rik van der Kleij
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

package intellij.haskell.external.execution

import com.intellij.psi.PsiFile
import intellij.haskell.util.{HaskellFileUtil, StringUtil}

object HaskellCompilationResultHelper {

  private final val ProblemPattern = """((?:[A-Z]:\\)?[^:]+):([\d]+):([\d]+):(.+)""".r

  final val LayoutSpaceChar = '\u00A0'

  def createCompilationResult(currentPsiFile: PsiFile, errorLines: Seq[String], failed: Boolean): CompilationResult = {
    val currentFilePath = HaskellFileUtil.getAbsolutePath(currentPsiFile).getOrElse(throw new IllegalStateException(s"File `${currentPsiFile.getName}` exists only in memory"))

    val compilationProblems = errorLines.flatMap(parseErrorLine)

    val (currentFileProblems, otherFileProblems) = compilationProblems.partition(_.filePath == currentFilePath)

    CompilationResult(currentFileProblems, otherFileProblems, failed)
  }

  def parseErrorLine(errorLine: String): Option[CompilationProblem] = {
    errorLine match {
      case ProblemPattern(filePath, lineNr, columnNr, message) =>
        val displayMessage = message.trim.replaceAll("""(\s\s\s\s+)""", "\n" + "$1")
        Some(CompilationProblem(filePath, lineNr.toInt, columnNr.toInt, displayMessage))
      case _ => None
    }
  }
}

case class CompilationResult(currentFileProblems: Iterable[CompilationProblem], otherFileProblems: Iterable[CompilationProblem], failed: Boolean)

case class CompilationProblem(filePath: String, lineNr: Int, columnNr: Int, message: String) {

  import intellij.haskell.external.execution.HaskellCompilationResultHelper.LayoutSpaceChar

  def plainMessage: String = {
    message.split("\n").mkString.replaceAll("\\s+", " ")
  }

  def htmlMessage: String = {
    StringUtil.escapeString(message.replace(' ', LayoutSpaceChar))
  }

  def isWarning: Boolean = {
    message.startsWith("warning:") || message.startsWith("Warning:")
  }
}


