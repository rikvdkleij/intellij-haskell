package intellij.haskell.repl

import java.nio.charset.Charset

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil

object HaskellConsoleProcessHandler {
  private def processPrompts(console: LanguageConsoleImpl, text: Option[String]): Option[String] = {
    text.map(text => {
      if (text.matches(HaskellConsoleHighlightingUtil.LineWithPrompt)) {
        val matcher = HaskellConsoleHighlightingUtil.GHCIPattern.matcher(text)
        matcher.find
        val prefix = matcher.group
        val trimmed = StringUtil.trimStart(text, prefix).trim
        console.setPrompt(prefix.replace(HaskellConsoleHighlightingUtil.PromptArrow, HaskellConsoleHighlightingUtil.LambdaArrow))
        trimmed
      } else {
        text
      }
    })
  }
}

final class HaskellConsoleProcessHandler private[repl](val process: Process, val commandLine: String, val console: LanguageConsoleImpl)
  extends ColoredProcessHandler(process, commandLine, Charset.forName("UTF-8")) {
  override def coloredTextAvailable(text: String, attributes: Key[_]) {
    HaskellConsoleProcessHandler.processPrompts(console, Option(StringUtil.convertLineSeparators(text))).foreach(string => {
      super.coloredTextAvailable(string, attributes)
    })
  }

  def getLanguageConsole: LanguageConsoleImpl = console
}
