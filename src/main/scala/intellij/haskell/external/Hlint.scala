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

import com.intellij.psi.PsiFile
import intellij.haskell.settings.HaskellSettingsState
import spray.json.{DefaultJsonProtocol, _}

object Hlint {

  def check(psiFile: PsiFile): Seq[HlintInfo] = {
    val hlintPath = HaskellSettingsState.getHlintPath
    hlintPath match {
      case Some(p) =>
        val output = ExternalProcess.getProcessOutput(
          psiFile.getProject.getBasePath,
          p,
          Seq("--json", psiFile.getVirtualFile.getPath)
        )
        val infos = deserializeHlintInfo(output.getStdout)
        infos.map((info: HlintInfo) => {
          val found = info.from
          val suggest = info.to.getOrElse("")
          val hint = List(info.hint, "Found:", found, "Why not:", suggest).mkString("\n")
          HlintInfo(
            info.module,
            info.decl,
            info.severity,
            hint,
            info.file,
            info.startLine,
            info.startColumn,
            info.endLine,
            info.endColumn,
            info.from,
            info.to,
            info.note
          )
        })
      case None => Seq()
    }
  }

  object HlintJsonProtocol extends DefaultJsonProtocol {
    implicit val hlintInfoFormat = jsonFormat12(HlintInfo)
  }

  import intellij.haskell.external.Hlint.HlintJsonProtocol._

  private[external] def deserializeHlintInfo(hlintInfo: String) = {
    if (hlintInfo.trim.isEmpty || hlintInfo == "[]") {
      Seq()
    } else {
      hlintInfo.parseJson.convertTo[Seq[HlintInfo]]
    }
  }
}

case class HlintInfo(module: Option[String], decl: Option[String], severity: String, hint: String, file: String, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, from: String = "", to: Option[String], note: Seq[String])

