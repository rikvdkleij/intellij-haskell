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

package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import intellij.haskell.external.commandLine.{CommandLine, StackCommandLine}
import intellij.haskell.external.component.HLintComponent
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellEditorUtil

import scala.collection.mutable.ArrayBuffer

class AboutAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableStackAction(actionEvent)
  }

  private def boldToolName(name: String): String = {
    if (SystemInfo.isMac) {
      s"<b>$name</b>"
    } else {
      name
    }
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    val messages = new ArrayBuffer[String]
    val project = actionEvent.getProject
    messages.+=(s"${boldToolName("Stack")} version: " + StackCommandLine.runCommand(Seq("--numeric-version"), project).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("GHC")} version: " + StackCommandLine.runCommand(Seq("exec", "--", "ghc", "--version"), project).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Intero")} version: " + StackCommandLine.runCommand(Seq("exec", "--", "intero", "--version"), project).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("HLint")} version: " + StackCommandLine.runCommand(Seq("exec", "--", HLintComponent.HlintName, "--version"), project).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Hoogle")} version: " + StackCommandLine.runCommand(Seq("exec", "--", "hoogle", "--version"), project).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Hindent")} version: " + HaskellSettingsState.getHindentPath(project).flatMap(hp =>
      CommandLine.runProgram(None, project.getBasePath, hp, Seq("--version")).map(_.getStdout)).getOrElse("-"))
    messages.+=(s"${boldToolName("Stylish-haskell")} version: " + HaskellSettingsState.getStylishHaskellPath(project).flatMap(sh =>
      CommandLine.runProgram(None, project.getBasePath, sh, Seq("--version")).map(_.getStdout)).getOrElse("-"))
    Messages.showInfoMessage(project, messages.mkString("\n"), "About Haskell Project")
  }
}
