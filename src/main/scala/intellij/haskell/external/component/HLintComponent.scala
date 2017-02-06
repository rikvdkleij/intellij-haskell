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

package intellij.haskell.external.component

import java.util.concurrent.Future

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, _}

object HLintComponent {

  final val HlintName = "hlint"
  var hlintAvailable = false

  def buildHlint(project: Project): Future[_] = {
    hlintAvailable = false
    ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
      override def run(): Unit = {
        try {
          StackCommandLine.executeBuild(project, Seq("build", HLintComponent.HlintName), "Build of `hlint`")
        } finally {
          hlintAvailable = true
        }
      }
    })
  }

  def check(psiFile: PsiFile): Seq[HLintInfo] = {
    if (hlintAvailable) {
      val project = psiFile.getProject
      StackCommandLine.runCommand(Seq("exec", "--", HlintName, "--json", psiFile.getOriginalFile.getVirtualFile.getPath), project).map(output => {
        if (output.getStderr.contains("Executable named hlint not found on path")) {
          HaskellNotificationGroup.logWarningEvent(project, s"$HlintName is not available yet")
          Seq()
        }
        else if (output.getStderr.nonEmpty) {
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Something went wrong while calling <b>$HlintName</b>. Error: ${output.getStderr}")
          Seq()
        } else {
          deserializeHLintInfo(project, output.getStdout)
        }
      }).getOrElse(Seq())
    } else {
      Seq()
    }
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

case class HLintInfo(module: Option[String], decl: Option[String], severity: String, hint: String, file: String, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, from: String = "", to: Option[String], note: Seq[String])
