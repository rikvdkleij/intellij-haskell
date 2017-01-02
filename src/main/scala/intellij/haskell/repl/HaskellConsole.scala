package intellij.haskell.repl

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellFileType

final class HaskellConsole private[repl](val project: Project, val title: String)
  extends LanguageConsoleImpl(project, title, HaskellFileType.INSTANCE.getLanguage) {}
