/*
 * Copyright 2012-2014 Sergey Ignatov
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
package intellij.haskell.runconfig.console

import java.io.{IOException, OutputStreamWriter}

import com.intellij.execution.console.{ConsoleHistoryController, ConsoleRootType, LanguageConsoleImpl}
import com.intellij.execution.filters._
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.{Project, ProjectManager}
import com.intellij.openapi.util.{Key, TextRange}
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import intellij.haskell.{HaskellFileType, HaskellNotificationGroup}

object HaskellConsoleView {
  private val HaskellConsoleKey: Key[HaskellConsoleInfo] = Key.create("HASKELL CONSOLE KEY")

  def isConsoleFile(file: PsiFile): Boolean = file.getOriginalFile.getUserData(HaskellConsoleKey) != null

  def findConsoleInfo(psiFile: PsiFile): Option[HaskellConsoleInfo] = {
    Option(psiFile.getOriginalFile.getUserData(HaskellConsoleKey))
  }
}

class HaskellConsoleView(val project: Project, val configuration: HaskellConsoleConfiguration) extends LanguageConsoleImpl(project, "Haskell Stack REPL", HaskellFileType.Instance.getLanguage) {

  private val consoleRootType = new ConsoleRootType("haskell", "Haskell") {}
  private var historyController: ConsoleHistoryController = _
  private var outputStreamWriter: OutputStreamWriter = _
  private var lastCommand: Option[String] = None

  addMessageFilter(createFileHyperLinkFilter())
  setPrompt(HaskellConsoleHighlightingUtil.LambdaArrow)

  val originalFile: PsiFile = getFile.getOriginalFile
  originalFile.putUserData(HaskellConsoleView.HaskellConsoleKey, HaskellConsoleInfo(configuration.getStackTarget, configuration.getName))

  override def attachToProcess(processHandler: ProcessHandler): Unit = {
    super.attachToProcess(processHandler)
    Option(processHandler.getProcessInput).foreach(processInput => {
      outputStreamWriter = new OutputStreamWriter(processInput)
      historyController = new ConsoleHistoryController(consoleRootType, "haskell", this)
      historyController.install()
      HaskellConsoleViewMap.addConsole(this)
    })
  }

  override def dispose() {
    super.dispose()
    HaskellConsoleViewMap.delConsole(this)
  }

  def append(text: String) {
    WriteCommandAction.runWriteCommandAction(getProject, new Runnable {
      override def run(): Unit = {
        val document = getCurrentEditor.getDocument
        document.insertString(document.getTextLength, text)
      }
    })
  }

  def execute(): Unit = {
    val consoleEditor = getConsoleEditor
    val editorDocument = consoleEditor.getDocument
    val text = editorDocument.getText

    DocumentUtil.writeInRunUndoTransparentAction(() => consoleEditor.getDocument.deleteString(0, text.length))

    executeCommand(text)
  }

  def executeCommand(command: String, silent: Boolean = false): Unit = {
    for {
      processInputWriter <- Option(outputStreamWriter)
      historyController <- Option(historyController)
    } yield {
      if (!silent) {
        lastCommand = Some(command)
        historyController.addToHistory(command)
        print(command + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
      }

      for (line <- command.split("\n")) {
        try {
          processInputWriter.write(line + "\n")
          processInputWriter.flush()
        } catch {
          case e: IOException => HaskellNotificationGroup.logErrorEvent(ProjectManager.getInstance().getDefaultProject, e.getMessage)
        }
      }
    }
  }

  def executeLastCommand(): Unit = {
    lastCommand.foreach { command =>
      executeCommand(command, silent = true)
    }
  }

  private def createFileHyperLinkFilter(): PatternBasedFileHyperlinkFilter = {
    val pattern = ".*?(?<file>(?:\\p{Alpha}\\:|/)[0-9 a-z_A-Z\\-\\\\./]+):(?<line>[0-9]+):(?<column>[0-9]+).*".r
    val format = new PatternHyperlinkFormat(pattern.pattern, false, false, PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN)
    val dataFinder = new PatternBasedFileHyperlinkRawDataFinder(Array(format))
    new PatternBasedFileHyperlinkFilter(project, null, dataFinder)
  }

}

final case class HaskellConsoleInfo(stackTarget: String, configurationName: String)