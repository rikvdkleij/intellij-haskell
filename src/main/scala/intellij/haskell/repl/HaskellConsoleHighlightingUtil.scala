package intellij.haskell.repl

import java.util.regex.Pattern

object HaskellConsoleHighlightingUtil {
  private val ID = "\\p{Lu}[\\p{Ll}\\p{Digit}]*"
  private val Module = "\\*?" + ID + "(\\." + ID + ")*"
  private val Modules = "(" + Module + "\\s*)*"
  private val PromptArrow = ">"
  val LineWithPrompt: String = Modules + PromptArrow + ".*"
  val GHCIPattern: Pattern = Pattern.compile(Modules + PromptArrow)
}
