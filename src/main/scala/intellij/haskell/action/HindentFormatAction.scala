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

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.util.concurrent.Callable

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle._
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.{FutureUtil, HaskellEditorUtil, HaskellFileUtil}
import intellij.haskell.{HaskellLanguage, HaskellNotificationGroup}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

sealed case class SelectionContext(start: Int, end: Int, text: String)

class HindentFormatAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val psiFile = actionContext.psiFile
      val selectionContext = actionContext.selectionModel.map(m =>
        HindentFormatAction.translateSelectionModelToSelectionContext(m))
      HindentFormatAction.format(psiFile, selectionContext)
    })
  }
}

object HindentFormatAction {
  final val HindentName = "hindent"

  def format(psiFile: PsiFile, selectionContext: Option[SelectionContext] = None): Unit = {
    val lineLength = CodeStyleSettingsManager.getInstance(psiFile.getProject).getCurrentSettings.getRightMargin(HaskellLanguage.Instance)
    val indentOptions = CodeStyleSettingsManager.getInstance(psiFile.getProject).getCurrentSettings.getCommonSettings(HaskellLanguage.Instance).getIndentOptions
    val virtualFile = HaskellFileUtil.findVirtualFile(psiFile)
    val project = psiFile.getProject
    HaskellFileUtil.saveFile(virtualFile)

    HaskellSettingsState.getHindentPath(project) match {
      case Some(hindentPath) =>
        val command = Seq(hindentPath, "--line-length", lineLength.toString, "--indent-size", indentOptions.INDENT_SIZE.toString)

        val formatAction = ApplicationManager.getApplication.executeOnPooledThread(new Callable[Either[String, String]] {
          override def call(): Either[String, String] = {
            selectionContext match {
              case Some(sc) => writeToHindent(command, sc.text)
              case None => writeToHindent(command, psiFile.getText)
            }
          }
        })

        val formattedSourceCode = FutureUtil.getValue(formatAction, project, s"formatting by `$HindentName`")
        formattedSourceCode.foreach {
          case Left(e) =>
            HaskellNotificationGroup.logErrorEvent(project, e)
            HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error while formatting by <b>$HindentName</b>. Error: $e")
          case Right(sourceCode) =>
            selectionContext match {
              case Some(sc) => HaskellFileUtil.saveFileWithPartlyNewContent(psiFile.getProject, virtualFile, sourceCode, sc)
              case None => HaskellFileUtil.saveFileWithNewContent(psiFile.getProject, virtualFile, sourceCode)
            }
        }

      case _ => HaskellNotificationGroup.logWarningEvent(project, s"Can not format code because path to `$HindentName` is not configured in IntelliJ")
    }
  }

  def translateSelectionModelToSelectionContext(selectionModel: SelectionModel): SelectionContext = {
    SelectionContext(selectionModel.getSelectionStart, selectionModel.getSelectionEnd, getSelectedText(selectionModel))
  }

  private def getSelectedText(selectionModel: SelectionModel) = {
    ApplicationManager.getApplication.runReadAction(new Computable[String] {
      override def compute(): String = selectionModel.getSelectedText
    })
  }

  private def writeToHindent(command: Seq[String], sourceCode: String): Either[String, String] = {
    val builder = new ProcessBuilder(command.asJava)
    val process = builder.start()

    val stdout = process.getInputStream
    val stderr = process.getErrorStream
    val stdin = process.getOutputStream

    val reader = new BufferedReader(new InputStreamReader(stdout))
    val errorReader = new BufferedReader(new InputStreamReader(stderr))
    val writer = new BufferedWriter(new OutputStreamWriter(stdin))

    writer.write(sourceCode)
    writer.flush()
    writer.close()

    val buffer = ListBuffer[String]()
    val result = read(reader, buffer).mkString("\n")

    val errorBuffer = ListBuffer[String]()
    val errors = read(errorReader, errorBuffer).mkString("\n")
    if (errors.isEmpty) {
      Right(result)
    } else {
      Left(errors)
    }
  }

  @tailrec
  private def read(reader: BufferedReader, buffer: ListBuffer[String]): ListBuffer[String] = {
    val line = reader.readLine()
    if (line == null) {
      buffer
    } else {
      buffer += line
      read(reader, buffer)
    }
  }
}

