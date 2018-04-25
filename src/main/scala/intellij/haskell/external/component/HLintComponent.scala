/*
 * Copyright 2014-2017 Rik van der Kleij
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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, _}

object HLintComponent {

  final val HLintName = "hlint"
  private final val HLintPath = GlobalInfo.toolPath(HLintName)

  def check(psiFile: PsiFile): Seq[HLintInfo] = {
    if (StackProjectManager.isHlintAvailable(psiFile.getProject)) {
      val project = psiFile.getProject
      val hlintOptions = if (HaskellSettingsState.getHlintOptions.trim.isEmpty) Array[String]() else HaskellSettingsState.getHlintOptions.split("""\s+""")
      val output = runHLint(project, hlintOptions.toSeq ++ Seq("--json", HaskellFileUtil.getAbsolutePath(psiFile)), ignoreExitCode = true)
      if (output.getExitCode > 0 && output.getStderr.nonEmpty) {
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while calling $HLintName: ${output.getStderr}")
        Seq()
      } else {
        deserializeHLintInfo(project, output.getStdout)
      }
    } else {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"$HLintName is not yet available")
      Seq()
    }
  }

  def versionInfo(project: Project): String = {
    runHLint(project, Seq("--version"), ignoreExitCode = false).getStdout
  }

  private def runHLint(project: Project, arguments: Seq[String], ignoreExitCode: Boolean) = {
    CommandLine.run(Some(project), project.getBasePath, HLintPath, arguments, logOutput = true, ignoreExitCode = ignoreExitCode)
  }

  private object HlintJsonProtocol extends DefaultJsonProtocol {
    implicit val hlintInfoFormat: RootJsonFormat[HLintInfo] = jsonFormat12(HLintInfo)
  }

  import intellij.haskell.external.component.HLintComponent.HlintJsonProtocol._

  private[external] def deserializeHLintInfo(project: Project, hlintInfo: String) = {
    if (hlintInfo.trim.isEmpty || hlintInfo == "[]") {
      Seq()
    } else {
      try {
        hlintInfo.parseJson.convertTo[Seq[HLintInfo]]
      } catch {
        case e: ParsingException =>
          HaskellNotificationGroup.logErrorEvent(project, s"Error ${e.getMessage} while parsing $hlintInfo")
          Seq()
      }
    }
  }
}

case class HLintInfo(module: Seq[String], decl: Seq[String], severity: String, hint: String, file: String, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, from: String = "", to: Option[String], note: Seq[String])
