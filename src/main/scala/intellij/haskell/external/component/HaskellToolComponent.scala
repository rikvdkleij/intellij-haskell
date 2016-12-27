package intellij.haskell.external.component

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine

object HaskellToolComponent {
  private final val HaskellToolName = "ht-refact"

  private def getCommandOptions(project: Project, moduleName:String, mode: String): Seq[String] = {
    Seq("exec", "--", HaskellToolName, "-one-shot", s"-module-name=$moduleName", s"-refactoring=$mode", project.getBasePath)
  }

  def generateExports(project: Project, moduleName: String): Unit = {
    StackCommandLine.runCommand(getCommandOptions(project, moduleName, "GenerateExports"), project).foreach(output => {
      if (output.getStderr.nonEmpty) {
        if (output.getStderr.toLowerCase.contains("couldn't find file: ht-refact")) {
          HaskellNotificationGroup.logWarningBalloonEvent(project, "Please use `cabal install haskell-tools-cli` to install haskell-tools command line interface first")
        }
      }
    })
  }
}
