package intellij.haskell.repl

import java.util.regex.Pattern

object HaskellConsoleHighlightingUtil {
  private val ID = "\\p{Lu}[\\p{Ll}\\p{Digit}]*"
  private val Module = s"\\*?$ID(\\.$ID)*"
  private val Modules = s"($Module\\s*)*"
  private val PromptArrow = ">"
  val LineWithPrompt: String = s"$Modules$PromptArrow.*"
  val GHCIPattern: Pattern = Pattern.compile(Modules + PromptArrow)
}
