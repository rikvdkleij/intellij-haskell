package intellij.haskell.runconfig.console

import java.nio.charset.Charset

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.{Computable, Key}
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.HaskellFile
import intellij.haskell.util.index.HaskellModuleNameIndex

object HaskellConsoleProcessHandler {

  private final val ModulesLoadedPattern = """(?s)Ok, modules loaded: (.+)""".r

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

  override def coloredTextAvailable(text: String, attributes: Key[_]) {
    text match {
      case HaskellConsoleProcessHandler.ModulesLoadedPattern(moduleNamesList) =>
        val moduleNames = moduleNamesList.trim.init.split(",").map(_.trim)
        moduleNames match {
          case Array(mn) =>
            val haskellFile = ApplicationManager.getApplication.runReadAction(new Computable[Option[HaskellFile]] {
              override def compute(): Option[HaskellFile] = {
                HaskellModuleNameIndex.findHaskellFileByModuleName(console.getProject, mn, GlobalSearchScope.projectScope(console.getProject))
              }
            })
            haskellFile.foreach(hf => HaskellConsoleViewMap.projectFileByConfigName.put(console.configuration.getName, hf))
          case _ => ()
        }
      case _ => ()
    }

    HaskellConsoleProcessHandler.processPrompts(console, Option(StringUtil.convertLineSeparators(text))).foreach(string => {
      super.coloredTextAvailable(string, attributes)
    })
  }

  def getLanguageConsole: LanguageConsoleImpl = console
}
