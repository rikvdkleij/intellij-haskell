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
package intellij.haskell.runconfig

import java.io.{IOException, OutputStreamWriter}

import com.intellij.execution.console.{ConsoleHistoryController, ConsoleRootType, LanguageConsoleImpl}
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.{Project, ProjectManager}
import com.intellij.openapi.util.TextRange
import intellij.haskell.{HaskellFileType, HaskellNotificationGroup}


final class HaskellConsoleView(val project: Project)
  extends LanguageConsoleImpl(project, "Haskell Stack REPL", HaskellFileType.INSTANCE.getLanguage) {

  private val myType = new ConsoleRootType("haskell", "Haskell") {}
  private var myHistoryController: ConsoleHistoryController = _
  private var myProcessInputWriter: OutputStreamWriter = _

  setPrompt("λ")

  override def attachToProcess(processHandler: ProcessHandler): Unit = {
    super.attachToProcess(processHandler)
    Option(processHandler.getProcessInput).foreach(processInput => {
      myProcessInputWriter = new OutputStreamWriter(processInput)
      myHistoryController = new ConsoleHistoryController(myType, "haskell", this)
      myHistoryController.install()
      HaskellConsoleViewDict.getInstance.addConsole(this)
    })
  }

  override def dispose() {
    super.dispose()
    HaskellConsoleViewDict.getInstance.delConsole(this)
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
    for {
      myProcessInputWriter <- Option(myProcessInputWriter)
      myHistoryController <- Option(myHistoryController)
    } yield {
      val consoleEditor = getConsoleEditor
      val editorDocument = consoleEditor.getDocument
      val text = editorDocument.getText

      addToHistoryInner(new TextRange(0, text.length), consoleEditor, true, true)
      myHistoryController.addToHistory(text)
      for (line <- text.split("\n")) {
        try {
          myProcessInputWriter.write(line + "\n")
          myProcessInputWriter.flush()
        } catch {
          case e: IOException => HaskellNotificationGroup.logErrorEvent(ProjectManager.getInstance().getDefaultProject, e.getMessage)
        }
      }
    }
  }
}
