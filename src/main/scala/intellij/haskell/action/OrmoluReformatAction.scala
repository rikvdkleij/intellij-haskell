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
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.util._
import intellij.haskell.{HTool, HaskellNotificationGroup}

class OrmoluReformatAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => StackProjectManager.isOrmoluAvailable(project).isDefined)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach { actionContext =>
      val psiFile = actionContext.psiFile
      OrmoluReformatAction.reformat(psiFile)
    }
  }
}

object OrmoluReformatAction {

  def reformat(psiFile: PsiFile): Boolean = {
    val project = psiFile.getProject
    StackProjectManager.isOrmoluAvailable(project) match {
      case Some(ormoluPath) =>
        HaskellFileUtil.saveFile(psiFile)

        HaskellFileUtil.getAbsolutePath(psiFile) match {
          case Some(path) =>
            val processOutputFuture = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable[ProcessOutput] {
              CommandLine.run(project, ormoluPath, Seq(path))
            })

            FutureUtil.waitForValue(project, processOutputFuture, s"reformatting by ${HTool.Ormolu.name}") match {
              case None => false
              case Some(processOutput) =>
                if (processOutput.getStderrLines.isEmpty) {
                  HaskellFileUtil.saveFileWithNewContent(psiFile, processOutput.getStdout)
                  true
                } else {
                  HaskellNotificationGroup.logInfoEvent(project, s"Error while reformatting by `${HTool.Ormolu.name}`. Error: ${processOutput.getStderr}")
                  false
                }
            }
          case None =>
            HaskellNotificationGroup.logWarningBalloonEvent(psiFile.getProject, s"Can not reformat file because could not determine path for file `${psiFile.getName}`. File exists only in memory")
            false
        }
      case None =>
        HaskellNotificationGroup.logWarningBalloonEvent(psiFile.getProject, s"Can not reformat file because `${HTool.Ormolu.name}` is not (yet) available")
        false
    }
  }

  def versionInfo(project: Project): String = {
    StackProjectManager.isOrmoluAvailable(project) match {
      case Some(ormoluPath) => CommandLine.run(project, ormoluPath, Seq("--version")).getStdout
      case None => "-"
    }
  }
}
