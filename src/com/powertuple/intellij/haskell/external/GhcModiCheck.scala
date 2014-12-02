/*
 * Copyright 2014 Rik van der Kleij
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

object GhcModiCheck {

  def check(project: Project, filePath: String): GhcModiCheckResult = {
    val ghcModi = GhcModiManager.getGhcModi(project)
    val ghcModiOutput = ghcModi.execute("check " + filePath)

    if (ghcModiOutput.outputLines.isEmpty) {
      new GhcModiCheckResult(Seq())
    } else {
      new GhcModiCheckResult(ghcModiOutput.outputLines.map(parseGhcModiOutputLine))
    }
  }

  private[external] def parseGhcModiOutputLine(ghcModOutput: String): GhcModiProblem = {
    val ghcModProblemPattern = """(.+):([\d]+):([\d]+):(.+)""".r
    val ghcModProblemPattern(filePath, lineNr, columnNr, description) = ghcModOutput
    new GhcModiProblem(filePath, lineNr.toInt, columnNr.toInt, description.replace("\u0000", "\n"))
  }
}

case class GhcModiCheckResult(problems: Seq[GhcModiProblem] = Seq())

case class GhcModiProblem(filePath: String, lineNr: Int, columnNr: Int, description: String)
