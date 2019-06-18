package intellij.haskell.action.ghci

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.action.ActionUtil
import intellij.haskell.runconfig.console.HaskellConsoleViewMap
import intellij.haskell.util.HaskellFileUtil

class LoadModuleAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    actionEvent.getPresentation.setEnabled(HaskellConsoleViewMap.getConsole(actionEvent.getProject).isDefined)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
    for {
      actionContext <- ActionUtil.findActionContext(actionEvent)
      consoleView <- HaskellConsoleViewMap.getConsole(actionContext.project)
    } yield {
      HaskellFileUtil.getAbsolutePath(actionContext.psiFile) match {
        case Some(filePath) => consoleView.executeCommand(s":load $filePath", addToHistory = false)
        case None => HaskellNotificationGroup.logWarningBalloonEvent(actionContext.project, s"Can't load file in REPL because `${actionContext.psiFile.getName}` only exists in memory")
      }
    }
  }

}
