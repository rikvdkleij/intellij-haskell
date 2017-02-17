package intellij.haskell.runconfig.console

import java.nio.charset.Charset

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil

object HaskellConsoleProcessHandler {
  private def processPrompts(console: LanguageConsoleImpl, text: Option[String]): Option[String] = {
    text.map(text => {
      HaskellConsoleHighlightingUtil.LineWithPrompt.findFirstIn(text) match {
        case Some(prefix) => StringUtil.trimStart(text, prefix)
        case None => text
      }
    })
  }
}

final class HaskellConsoleProcessHandler private[runconfig](val process: Process, val commandLine: String, val console: LanguageConsoleImpl)
  extends ColoredProcessHandler(process, commandLine, Charset.forName("UTF-8")) {
  override def coloredTextAvailable(text: String, attributes: Key[_]) {
    HaskellConsoleProcessHandler.processPrompts(console, Option(StringUtil.convertLineSeparators(text))).foreach(string => {
      super.coloredTextAvailable(string, attributes)
    })
  }

  def getLanguageConsole: LanguageConsoleImpl = console
}
