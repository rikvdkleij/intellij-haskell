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
import intellij.haskell.util.StackUtil
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, _}

object HLintComponent {

  final val HlintName = "hlint"

  def check(psiFile: PsiFile): Seq[HLintInfo] = {
    val output = StackUtil.runCommand(Seq("exec", "--", HlintName, "--json", psiFile.getOriginalFile.getVirtualFile.getPath), psiFile.getProject)
    if (output.getStderr.nonEmpty) {
      if (output.getStderr.toLowerCase.contains("couldn't find file: hlint")) {
        HaskellNotificationGroup.notifyBalloonWarning("No Hlint suggestions because `hlint` build still has to be started or build is not finished yet")
      }
    }
    deserializeHLintInfo(output.getStdout)
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
