/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._
import intellij.haskell.{HTool, HaskellNotificationGroup}

class OrmoluReformatAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => HaskellReformatAction.reformatByOrmolu)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach { actionContext =>
      val psiFile = actionContext.psiFile
      HaskellSettingsState.ormoluPath match {
        case Some(p) => OrmoluReformatAction.format(psiFile, p)
        case None => ()
      }
    }
  }
}

object OrmoluReformatAction {

  def format(psiFile: PsiFile, ormoluPath: String): Unit = {
    val project = psiFile.getProject
    HaskellFileUtil.saveFile(psiFile)

    HaskellFileUtil.getAbsolutePath(psiFile) match {
      case Some(path) =>
        val processOutputFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable[ProcessOutput] {
          CommandLine.run(project, ormoluPath, Seq(path))
        })

        FutureUtil.waitForValue(project, processOutputFuture, s"reformatting by ${HTool.Ormolu.name}") match {
          case None => ()
          case Some(processOutput) =>
            if (processOutput.getStderrLines.isEmpty) {
              HaskellFileUtil.saveFileWithNewContent(psiFile, processOutput.getStdout)
            } else {
              HaskellNotificationGroup.logInfoEvent(project, s"Error while reformatting by `${HTool.Ormolu.name}`. Error: ${processOutput.getStderr}")
            }
        }
      case None => HaskellNotificationGroup.logWarningBalloonEvent(psiFile.getProject, s"Can not reformat file because could not determine path for file `${psiFile.getName}`. File exists only in memory")
    }
  }


  def versionInfo(project: Project): String = {
    HaskellSettingsState.ormoluPath.map(p => CommandLine.run(project, p, Seq("--version")).getStdout).getOrElse("-")
  }
}
