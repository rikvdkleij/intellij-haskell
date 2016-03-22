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

package intellij.haskell.external

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.util.{FileUtil, OSUtil}

object GhcModCheck {

  private val ghcModProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r

  def check(project: Project, filePath: String): GhcModCheckResult = {
    val output = GhcModProcessManager.getGhcModProcess(project).execute("check " + filePath)
    //    val ghcModPath = HaskellSettingsState.getGhcModPath
    //    val output = ghcModPath.map { p =>
    //      ExternalProcess.getProcessOutput(
    //        project.getBasePath,
    //        p,
    //        Seq("check", filePath)
    //      )
    //    }.map(_.getStdoutLines)

    new GhcModCheckResult(output.outputLines.flatMap(l => parseOutputLine(l, project)))
    //    match {
    //      case None => new GhcModCheckResult(Iterable())
    //      case Some(o) => new GhcModCheckResult(o.flatMap(l => parseOutputLine(l, project)))
    //    }
  }

  private[external] def parseOutputLine(ghcModOutput: String, project: Project): Option[GhcModProblem] = {
    ghcModOutput match {
      case ghcModProblemPattern(filePath, lineNr, columnNr, message) => Some(new GhcModProblem(FileUtil.makeFilePathAbsolute(filePath, project), lineNr.toInt, columnNr.toInt, message.replace('\u0000', OSUtil.LineSeparator)))
      case _ => HaskellNotificationGroup.notifyError(ghcModOutput); None
    }
  }
}

case class GhcModCheckResult(problems: Iterable[GhcModProblem] = Iterable())

case class GhcModProblem(filePath: String, lineNr: Int, columnNr: Int, message: String) {

  def getNormalizedMessage: String = {
    message.trim.replace(OSUtil.LineSeparator, ' ')
  }
}
