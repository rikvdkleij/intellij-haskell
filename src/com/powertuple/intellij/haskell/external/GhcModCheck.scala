/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.external

import com.intellij.openapi.project.Project
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.settings.HaskellSettings
import com.powertuple.intellij.haskell.util.{FileUtil, OSUtil}

import scala.collection.JavaConversions._

object GhcModCheck {

  private val ghcModProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r

  def check(project: Project, filePath: String): GhcModCheckResult = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("check", filePath)
    ).getStdoutLines

    if (output.isEmpty) {
      new GhcModCheckResult(Iterable())
    } else {
      new GhcModCheckResult(output.flatMap(o => parseOutputLine(o, project)))
    }
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
