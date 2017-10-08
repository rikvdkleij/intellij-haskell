package intellij.haskell.runconfig.console

import java.nio.charset.Charset

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.process.ColoredProcessHandler
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

class HaskellConsoleProcessHandler private[runconfig](val process: Process, val commandLine: String, val console: HaskellConsoleView) extends ColoredProcessHandler(process, commandLine, Charset.forName("UTF-8")) {

  def getLanguageConsole: LanguageConsoleImpl = console
}
