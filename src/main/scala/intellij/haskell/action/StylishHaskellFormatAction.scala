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
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.CommandLine
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil}

import scala.sys.process.ProcessLogger

class StylishHaskellFormatAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findPsiFile(actionEvent).foreach(psiFile => {
      StylishHaskellFormatAction.format(psiFile)
    })
  }

  class OnlyErrorProcessLogger extends ProcessLogger {
    override def out(s: => String): Unit = ()

    override def err(s: => String): Unit = HaskellNotificationGroup.logError(s)

    override def buffer[T](f: => T): T = f
  }

}

object StylishHaskellFormatAction {
  final val StylishHaskellName = "stylish-haskell"

  private[action] def format(psiFile: PsiFile): Unit = {
    val virtualFile = HaskellFileUtil.findVirtualFile(psiFile)

    HaskellFileUtil.saveFile(virtualFile)

    HaskellSettingsState.getStylishHaskellPath match {
      case Some(stylishHaskellPath) =>

        val processOutput = CommandLine.runCommand(psiFile.getProject.getBasePath, stylishHaskellPath, Seq(HaskellFileUtil.getFilePath(psiFile)))

        if (!processOutput.getStderrLines.isEmpty) {
          HaskellNotificationGroup.notifyBalloonError(s"Error while formatting by `$StylishHaskellName`. See Event Log for errors")
        }

        HaskellFileUtil.saveFileWithContent(psiFile.getProject, virtualFile, processOutput.getStdout)
      case _ => HaskellNotificationGroup.logWarning(s"Can not format code because path to `$StylishHaskellName` is not configured in IntelliJ")
    }
  }
}
