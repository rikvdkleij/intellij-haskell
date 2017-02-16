package intellij.haskell.runconfig

import scala.util.matching.Regex

object HaskellConsoleHighlightingUtil {
  private val ID = "[A-Z]\\w*"
  private val Module = s"\\*?$ID(\\.$ID)*"
  private val Modules = s"($Module\\s*)*"
  val PromptArrow = ">"
  val LambdaArrow = "λ> "
  val LineWithPrompt = new Regex(s"($Modules$PromptArrow)")
}
