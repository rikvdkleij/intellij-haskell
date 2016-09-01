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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.settings.HaskellSettingsState
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, _}

object HLintComponent {

  def check(psiFile: PsiFile): Seq[HLintInfo] = {
    val hlintPath = HaskellSettingsState.getHLintPath
    hlintPath match {
      case Some(p) =>
        val output = CommandLine.getProcessOutput(
          psiFile.getProject.getBasePath,
          p,
          Seq("--json", psiFile.getVirtualFile.getPath)
        )
        deserializeHLintInfo(output.getStdout)
      case None => Seq()
    }
  }

  object HlintJsonProtocol extends DefaultJsonProtocol {
    implicit val hlintInfoFormat = jsonFormat12(HLintInfo)
  }

  import intellij.haskell.external.HLintComponent.HlintJsonProtocol._

  private[external] def deserializeHLintInfo(hlintInfo: String) = {
    if (hlintInfo.trim.isEmpty || hlintInfo == "[]") {
      Seq()
    } else {
      try {
        hlintInfo.parseJson.convertTo[Seq[HLintInfo]]
      } catch {
        case e: ParsingException =>
          HaskellNotificationGroup.logInfo(s"Error ${e.getMessage} while parsing $hlintInfo")
          Seq()
      }
    }
  }
}

case class HLintInfo(module: Option[String], decl: Option[String], severity: String, hint: String, file: String, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, from: String = "", to: Option[String], note: Seq[String])

