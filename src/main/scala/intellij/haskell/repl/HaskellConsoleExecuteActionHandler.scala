package intellij.haskell.repl

import java.io.IOException

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.Result
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil

class HaskellConsoleExecuteActionHandler(val project: Project, var processHandler: ProcessHandler) {
  def runExecuteAction(console: LanguageConsoleImpl, executeImmediately: Boolean) {
    //ConsoleHistoryModel consoleHistoryModel = console.getHistoryModel();
    if (executeImmediately) {
      execute(console)
      return
    }
    // Process input and add to history
    val editor = console.getCurrentEditor
    val document = editor.getDocument
    val caretModel = editor.getCaretModel
    val offset = caretModel.getOffset
    val text = document.getText
    if ("" != text.substring(offset).trim) {
      val before = text.substring(0, offset)
      val after = text.substring(offset)
      val indent = 0
      val spaces = StringUtil.repeatSymbol(' ', indent)
      val newText = before + "\n" + spaces + after
      new WriteCommandAction[Nothing](project) {
        @throws[Throwable]
        protected def run(result: Result[Nothing]) {
          console.setInputText(newText)
          caretModel.moveToOffset(offset + indent + 1)
        }
      }.execute
      return
    }
    execute(console)
  }

  private def execute(languageConsole: LanguageConsoleImpl) {
    // Process input and add to history
    val document = languageConsole.getCurrentEditor.getDocument
    val text = document.getText
    val range = new TextRange(0, document.getTextLength)
    languageConsole.getCurrentEditor.getSelectionModel.setSelection(range.getStartOffset, range.getEndOffset)
    languageConsole.setInputText("")
    // Send to interpreter / server
    processLine(text)
  }

  private def processLine(line: String) {
    val os = processHandler.getProcessInput
    if (os != null) {
      val bytes = (line + "\n").getBytes
      try {
        os.write(bytes)
        os.flush()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
  }
}
