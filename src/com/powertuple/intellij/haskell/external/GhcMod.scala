/*
 * Copyright 2014 Rik van der Kleij

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

object GhcMod {

  import scala.collection.JavaConversions._

  def browseInfo(project: Project, moduleNames: Seq[String]): Seq[BrowseInfo] = {
    val output = ExternalProcess.getProcessOutput(
      project.getBasePath,
      HaskellSettings.getInstance().getState.ghcModPath,
      Seq("browse", "-d", "-q", "-o") ++ moduleNames
    )
    val browseInfos = output.getStdoutLines.map(_.split("::")).map(cols => {
      if (cols.size < 2) {
        None
      } else {
        val typeSignature = cols(1)
        cols(0).split('.') match {
          case Array() => None
          case Array(name) => Some(BrowseInfo(name, "", typeSignature))
          case names => Some(BrowseInfo(names.last, names.tail.mkString("."), typeSignature))
        }
      }
    })
    browseInfos.flatten
  }
}

case class BrowseInfo(name: String, moduleName: String, typeSignature: String)