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

import java.io._

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util._
import intellij.haskell.{GlobalInfo, HTool, HaskellLanguage, HaskellNotificationGroup}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

sealed case class SelectionContext(start: Int, end: Int, text: String)

class HindentReformatAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => StackProjectManager.isHindentAvailable(project))
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach { actionContext =>
      val psiFile = actionContext.psiFile
      val selectionContext = actionContext.selectionModel.map(HindentReformatAction.translateSelectionModelToSelectionContext)
      HindentReformatAction.format(psiFile, selectionContext)
    }
  }
}

object HindentReformatAction {
  private final val HindentPath = HaskellSettingsState.hindentPath.getOrElse(GlobalInfo.toolPath(HTool.Hindent).toString)

  def format(psiFile: PsiFile, selectionContext: Option[SelectionContext] = None): Boolean = {
    val lineLength = CodeStyle.getSettings(psiFile.getProject).getRightMargin(HaskellLanguage.Instance)
    val indentOptions = CodeStyle.getSettings(psiFile.getProject).getCommonSettings(HaskellLanguage.Instance).getIndentOptions
    val project = psiFile.getProject
    HaskellFileUtil.saveFile(psiFile)

    val command = Seq(HindentPath, "--line-length", lineLength.toString, "--indent-size", indentOptions.INDENT_SIZE.toString)

    val formatAction = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable[Either[String, String]] {
      selectionContext match {
        case Some(sc) => writeToHindent(command, sc.text, project)
        case None => writeToHindent(command, ApplicationUtil.runReadAction(psiFile.getText), project)
      }
    })

    FutureUtil.waitForValue(project, formatAction, s"reformatting by `${HTool.Hindent.name}`") match {
      case None => false
      case Some(r) => r match {
        case Left(e) =>
          HaskellNotificationGroup.logInfoEvent(project, s"Error while reformatting by `${HTool.Hindent.name}`. Error: $e")
          false
        case Right(sourceCode) =>
          selectionContext match {
            case Some(sc) => HaskellFileUtil.saveFileWithPartlyNewContent(psiFile, sourceCode, sc)
            case None => HaskellFileUtil.saveFileWithNewContent(psiFile, sourceCode)
          }
          true
      }
    }
  }

  def translateSelectionModelToSelectionContext(selectionModel: SelectionModel): SelectionContext = {
    SelectionContext(selectionModel.getSelectionStart, selectionModel.getSelectionEnd, getSelectedText(selectionModel))
  }

  def versionInfo(project: Project): String = {
    if (StackProjectManager.isHindentAvailable(project)) {
      CommandLine.run(project, HindentPath, Seq("--version")).getStdout
    } else {
      "-"
    }
  }

  private def getSelectedText(selectionModel: SelectionModel) = {
    selectionModel.getSelectedText
  }

  private def writeToHindent(command: Seq[String], sourceCode: String, project: Project): Either[String, String] = {
    val builder = new ProcessBuilder(command.asJava)
    builder.directory(new File(project.getBasePath))
    val process = builder.start()

    val stdout = process.getInputStream
    val stderr = process.getErrorStream
    val stdin = process.getOutputStream

    val reader = new BufferedReader(new InputStreamReader(stdout))
    val errorReader = new BufferedReader(new InputStreamReader(stderr))
    val writer = new BufferedWriter(new OutputStreamWriter(stdin))

    try {
      try {
        writer.write(sourceCode)
        writer.flush()
      } finally {
        writer.close()
      }

      val buffer = ListBuffer[String]()
      val result = read(reader, buffer).mkString("\n")

      val errorBuffer = ListBuffer[String]()
      val errors = read(errorReader, errorBuffer).mkString("\n")
      if (errors.isEmpty) {
        Right(result)
      } else {
        Left(errors)
      }
    } finally {
      errorReader.close()
      reader.close()
      stdin.close()
      stderr.close()
      stdout.close()

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
