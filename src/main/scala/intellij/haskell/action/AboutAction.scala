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

package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import intellij.haskell.external.component.{HLintComponent, StackProjectManager}
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellEditorUtil

import scala.collection.mutable.ArrayBuffer

class AboutAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableExternalAction(actionEvent, !StackProjectManager.isInitializing(_))
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
    messages.+=(s"${boldToolName("Stack")} version: " + StackCommandLine.run(project, Seq("--numeric-version")).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("GHC")}: " + StackCommandLine.run(project, Seq("exec", "--", "ghc", "--version")).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Intero")}: " + StackCommandLine.run(project, Seq("exec", "--", "intero", "--version")).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("HLint")}: " + StackCommandLine.run(project, Seq("exec", "--", HLintComponent.HlintName, "--version")).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Hoogle")}: " + StackCommandLine.run(project, Seq("exec", "--", "hoogle", "--version")).map(_.getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Hindent")}: " + HaskellSettingsState.getHindentPath(project).map(hp =>
      CommandLine.run(None, project.getBasePath, hp, Seq("--version")).getStdout).getOrElse("-"))
    messages.+=(s"${boldToolName("Stylish-haskell")}: " + HaskellSettingsState.getStylishHaskellPath(project).map(sh =>
      CommandLine.run(None, project.getBasePath, sh, Seq("--version")).getStdout).getOrElse("-"))
    Messages.showInfoMessage(project, messages.mkString("\n"), "About Haskell Project")
  }
}
