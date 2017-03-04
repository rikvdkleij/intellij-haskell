package intellij.haskell.external.component

import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.util.StackYamlUtil
import com.github.nscala_time.time.Imports._
import com.intellij.openapi.actionSystem.AnActionEvent
import intellij.haskell.action.ActionContext

object HaskellToolComponent {
  final val HaskellToolsCLIName = "haskell-tools-cli"
  private final val HaskellToolName = "ht-refact"

  private def getCommandOptions(project: Project, moduleName: String, mode: String): Seq[String] = {
    Seq("exec", "--", HaskellToolName, "-one-shot", s"-module-name=$moduleName", s"-refactoring=$mode", project.getBasePath)
  }

  private def getMillisOfDate(date: String): Long = {
    DateTime.parse(date).getMillis
  }

  def checkResolverForHaskellTools(project: Project): Boolean = {
    StackYamlUtil.getResolverFromStackYamlFile(project).exists(resolver => {
      if (resolver.startsWith("lts-")) {
        resolver.replace("lts-", "").toDouble >= 8.0
      } else if (resolver.startsWith("nightly-")) {
        getMillisOfDate(resolver.replace("nightly-", "")) > getMillisOfDate("2017-01-14")
      } else {
        false
      }
    })
  }

  def checkResolverForHaskellToolsAction(project: Project, actionEvent: AnActionEvent, act: AnActionEvent => Unit): Unit = {
    if (checkResolverForHaskellTools(project)) {
      act(actionEvent)
    } else {
      HaskellNotificationGroup.logWarningBalloonEvent(project, s"You need a Stack resolver greater than <b>lts-8.0</b> or <b>nightly-2017-01-14</b> in order to work with <b>$HaskellToolName</b>.")
    }
  }

  def generateExports(project: Project, moduleName: String): Unit = {
    StackCommandLine.runCommand(getCommandOptions(project, moduleName, "GenerateExports"), project).foreach(output => {
      if (output.getStderr.nonEmpty) {
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Something went wrong while calling <b>$HaskellToolName</b>. Error: ${output.getStderr}")
      }
    })
  }

  private def getSrcRange(selectionModel: SelectionModel): String = {
    val startPoint = selectionModel.getSelectionStartPosition
    val endPoint = selectionModel.getSelectionEndPosition

    s"${startPoint.line + 1}:${startPoint.column + 1}-${endPoint.line + 1}:${endPoint.column + 1}"
  }

  def extractBinding(project: Project, moduleName: String, selectionModel: SelectionModel, newName: String): Unit = {
    val mode = "\"ExtractBinding" + s" ${getSrcRange(selectionModel)} $newName" + "\""
    StackCommandLine.runCommand(getCommandOptions(project, moduleName, mode), project).foreach(output => {
      if (output.getStderr.nonEmpty) {
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Something went wrong while calling <b>$HaskellToolName</b>. Error: ${output.getStderr}")
      }
    })
  }

  def inlineBinding(project: Project, moduleName: String, selectionModel: SelectionModel): Unit = {
    val mode = "\"InlineBinding" + s" ${getSrcRange(selectionModel)}" + "\""
    StackCommandLine.runCommand(getCommandOptions(project, moduleName, mode), project).foreach(output => {
      if (output.getStderr.nonEmpty) {
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Something went wrong while calling <b>$HaskellToolName</b>. Error: ${output.getStderr}")
      }
    })
  }
}
