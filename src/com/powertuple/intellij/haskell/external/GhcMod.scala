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
import com.powertuple.intellij.haskell.settings.HaskellSettings

import scala.collection.JavaConversions._

object GhcMod {

  def browseInfo(project: Project, moduleNames: Seq[String], removeParensFromOperator: Boolean): Seq[BrowseInfo] = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("browse", "-d", "-q", "-o") ++ moduleNames
    )
    output.getStdoutLines.map(createBrowseInfo(_, None, removeParensFromOperator)).flatten
  }

  /**
   * Please note that returned browse info has explicitly same module name as given module name. This is done
   * so caller can in result refer to given module name. Especially done in case module imports other modules.
   */
  def browseInfo(project: Project, moduleName: String, removeParensFromOperator: Boolean): Seq[BrowseInfo] = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("browse", "-d", "-q", "-o") ++ Seq(moduleName)
    )
    output.getStdoutLines.map(createBrowseInfo(_, Some(moduleName), removeParensFromOperator)).flatten
  }

  private def createBrowseInfo(info: String, moduleName: Option[String], removeParensFromOperator: Boolean): Option[BrowseInfo] = {
    info.split("::") match {
      case Array(qn, d) =>
        val (m, n) = getModuleAndName(qn, removeParensFromOperator)
        Some(BrowseInfo(n, moduleName.getOrElse(m), d))
      case Array(qn) =>
        val (m, n) = getModuleAndName(qn, removeParensFromOperator)
        Some(BrowseInfo(n, moduleName.getOrElse(m), ""))
      case _ => None
    }
  }

  private def getModuleAndName(qualifiedName: String, removeParensFromOperator: Boolean): (String, String) = {
    val indexOfOperator = qualifiedName.lastIndexOf(".(") + 1
    if (indexOfOperator > 1) {
      val (m, o) = trimPair(qualifiedName.splitAt(indexOfOperator))
      (m.substring(0, m.length - 1), if (removeParensFromOperator) o.substring(1, o.length - 1) else o)
    } else {
      val indexOfId = qualifiedName.lastIndexOf('.') + 1
      val (m, id) = trimPair(qualifiedName.splitAt(indexOfId))
      (m.substring(0, m.length - 1), id)
    }
  }

  private def trimPair(t: (String, String)) = {
    t match {
      case (t0, t1) => (t0.trim, t1.trim)
    }
  }

  def listAvailableModules(project: Project): Seq[String] = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("list")
    )
    output.getStdoutLines
  }

  def check(project: Project, filePath: String): GhcModiOutput = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("check", filePath)
    )
    GhcModiOutput(output.getStdoutLines)
  }
}

case class BrowseInfo(name: String, moduleName: String, declaration: String)