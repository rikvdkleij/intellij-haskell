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
import intellij.haskell.external.commandLine.{CommandLine, StackCommandLine}
import intellij.haskell.external.component.HLintComponent
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellEditorUtil

import scala.collection.mutable.ArrayBuffer

class AboutAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val messages = new ArrayBuffer[String]
      messages.+=("Stack version: " + StackCommandLine.runCommand(Seq("--version"), actionContext.project).map(_.getStdout).getOrElse("-"))
      messages.+=("GHC version: " + StackCommandLine.runCommand(Seq("exec", "--", "ghc", "--version"), actionContext.project).map(_.getStdout).getOrElse("-"))
      messages.+=("Intero version: " + StackCommandLine.runCommand(Seq("exec", "--", "intero", "--version"), actionContext.project).map(_.getStdout).getOrElse("-"))
      messages.+=("HLint version: " + StackCommandLine.runCommand(Seq("exec", "--", HLintComponent.HlintName, "--version"), actionContext.project).map(_.getStdout).getOrElse("-"))
      messages.+=("Haskell-docs version can not be retrieved from command line\n")
      messages.+=("Hindent: " + HaskellSettingsState.getHindentPath.flatMap(hp =>
        CommandLine.runProgram(None, actionContext.project.getBasePath, hp, Seq("--version")).map(_.getStdout)).getOrElse("-"))
      messages.+=("Stylish-haskell: " + HaskellSettingsState.getStylishHaskellPath.flatMap(sh =>
        CommandLine.runProgram(None, actionContext.project.getBasePath, sh, Seq("--version")).map(_.getStdout)).getOrElse("-"))
      Messages.showInfoMessage(actionContext.project, messages.mkString("\n"), "About Haskell project")
    })
  }
}
