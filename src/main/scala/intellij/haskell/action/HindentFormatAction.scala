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

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.codeStyle._
import com.intellij.psi.util.PsiUtilBase
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil}
import intellij.haskell.{HaskellLanguage, HaskellNotificationGroup}

import scala.sys.process.ProcessLogger

class HindentFormatAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    val context = actionEvent.getDataContext
    val psiFile = for {
      editor <- Option(CommonDataKeys.EDITOR.getData(context))
      psiFile <- Option(PsiUtilBase.getPsiFileInEditor(editor, CommonDataKeys.PROJECT.getData(context)))
    } yield psiFile

    psiFile.foreach(pf => {
      val lineLength = CodeStyleSettingsManager.getInstance(pf.getProject).getCurrentSettings.getRightMargin(HaskellLanguage.Instance)
      val indentOptions = CodeStyleSettingsManager.getInstance(pf.getProject).getCurrentSettings.getCommonSettings(HaskellLanguage.Instance).getIndentOptions
      val virtualFile = HaskellFileUtil.findVirtualFile(pf)

      HaskellFileUtil.saveFile(virtualFile)

      HaskellSettingsState.getHindentPath match {
        case Some(hindentPath) =>
          val command = Seq(hindentPath, "--line-length", lineLength.toString, "--indent-size", indentOptions.INDENT_SIZE.toString)

          import scala.sys.process._
          val processBuilder = command #< virtualFile.getInputStream
          val formattedSourceCode = processBuilder.lineStream_!(new OnlyErrorProcessLogger).mkString("\n")

          save(pf.getProject, virtualFile, formattedSourceCode)
        case _ => HaskellNotificationGroup.logWarning("Can not format code because path to Hindent is not configured in IntelliJ")
      }
    })
  }

  private def save(project: Project, virtualFile: VirtualFile, sourceCode: String) = {
    CommandProcessor.getInstance().executeCommand(project, new Runnable {
      override def run(): Unit = {
        ApplicationManager.getApplication.runWriteAction(new Runnable {
          override def run(): Unit = {
            val document = HaskellFileUtil.findDocument(virtualFile)
            document.foreach(_.setText(sourceCode))
          }
        })
      }
    }, null, null)
  }

  class OnlyErrorProcessLogger extends ProcessLogger {
    override def out(s: => String): Unit = ()

    override def err(s: => String): Unit = HaskellNotificationGroup.logError(s)

    override def buffer[T](f: => T): T = f
  }

}

object HindentFormatAction {
  final val HindentName = "hindent"
}
