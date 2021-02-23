/*
 * Copyright 2014-2020 Rik van der Kleij
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
import intellij.haskell.{HTool, HaskellNotificationGroup}
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, _}

object HLintComponent {

  def check(psiFile: PsiFile): Seq[HLintInfo] = {
    StackProjectManager.isHlintAvailable(psiFile.getProject) match {
      case Some(hlintPath) =>
        val project = psiFile.getProject
        val hlintOptions = if (HaskellSettingsState.getHlintOptions.trim.isEmpty) Array[String]() else HaskellSettingsState.getHlintOptions.split("""\s+""")
        HaskellFileUtil.getAbsolutePath(psiFile) match {
          case Some(path) =>
            val output = runHLint(project, hlintPath, hlintOptions.toSeq ++ Seq("--json", path), ignoreExitCode = true)
            if (output.getExitCode > 0 && output.getStderr.nonEmpty) {
              HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while calling ${HTool.Hlint.name}: ${output.getStderr}")
              Seq()
            } else {
              parseHLintOutput(project, output.getStdout)
            }
          case None => ()
            HaskellNotificationGroup.logWarningBalloonEvent(psiFile.getProject, s"Can not display HLint suggestions because can not determine path for file `${psiFile.getName}`. File exists only in memory")
            Seq()
        }
      case None =>
        HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"${HTool.Hlint.name} is not (yet) available")
        Seq()
    }
  }

  def versionInfo(project: Project): String = {
    StackProjectManager.isHlintAvailable(project) match {
      case Some(hlintPath) => runHLint(project, hlintPath, Seq("--version"), ignoreExitCode = false).getStdout
      case None => "-"
    }
  }

  private def runHLint(project: Project, hlintPath: String, arguments: Seq[String], ignoreExitCode: Boolean) = {
    CommandLine.run(project, hlintPath, arguments, logOutput = true, ignoreExitCode = ignoreExitCode)
  }

  private object HlintJsonProtocol extends DefaultJsonProtocol {
    implicit val hlintInfoFormat: RootJsonFormat[HLintInfo] = jsonFormat13(HLintInfo)
  }

  import intellij.haskell.external.component.HLintComponent.HlintJsonProtocol._

  private[external] def parseHLintOutput(project: Project, hlintOutput: String) = {
    if (hlintOutput.trim.isEmpty || hlintOutput == "[]") {
      Seq()
    } else {
      try {
        hlintOutput.parseJson.convertTo[Seq[HLintInfo]]
      } catch {
        case e: ParsingException =>
          HaskellNotificationGroup.logErrorEvent(project, s"Error while parsing HLint output | Message: ${e.getMessage} | HLintOutput: $hlintOutput")
          Seq()
      }
    }
  }
}

case class HLintInfo(module: Seq[String], decl: Seq[String], severity: String, hint: String, file: String, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, from: String = "", to: Option[String], note: Seq[String], refactorings: String)
